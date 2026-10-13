package org.mcdatabase.openmcsurvey;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class OpenMCSurvey implements ModInitializer {
	public static final String MOD_ID = "openmcsurvey";
	public static final String COMMAND = "open-mc-survey";

	private static final String OPENMCDB_URL = "https://openmcdb.dpdns.org/";
	private static final String CONFIG_FILENAME = "openmcsurvey.json";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Config CONFIG = new Config(CONFIG_FILENAME);
	private static final OpenMCDBClient openMCDBClient = new OpenMCDBClient(OPENMCDB_URL);
	private static final ExecutorService SENDER = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, MOD_ID);
		t.setDaemon(true);
		return t;
	});

    public static boolean noUserConsent() {
        return !CONFIG.isOptedIn();
    }

    private static volatile Boolean optInAskedCache = null;
    private static final AtomicBoolean snapshotSentThisLaunch = new AtomicBoolean(false);

    public static boolean shouldShowOptIn() {
        Boolean cached = optInAskedCache;
        if (cached != null) {
            return !cached;
        }
        try {
            CONFIG.ensureExists();
        } catch (Exception e) {
            LOGGER.warn("Failed to create config file", e);
        }
        boolean asked = CONFIG.wasOptInAsked();
        optInAskedCache = asked;
        return !asked;
    }

    public static void setOptedIn(boolean value) {
        CONFIG.setOptedIn(value);
        CONFIG.markOptInAsked();
        optInAskedCache = true;
        if (value) {
            LOGGER.info("User opted in to openMCDB survey");
            collectAndSend();
        } else {
            LOGGER.info("User opted out of openMCDB survey");
        }
    }

    @Override
	public void onInitialize() {}

	public static void collectAndSend() {
		try {
			CONFIG.ensureExists();
		} catch (Exception e) {
			LOGGER.warn("Failed to create config file", e);
		}

		if (noUserConsent()) {
			LOGGER.info("Sent nothing to openMCDB, user consent not given");
			return;
		}

		if (!snapshotSentThisLaunch.compareAndSet(false, true)) {
			LOGGER.info("Snapshot already sent this launch, skipping duplicate");
			return;
		}

		final Snapshot snapshot;
		final boolean firstRequest = !CONFIG.hasSentFirst();
		try {
			snapshot = Surveyor.collect(firstRequest);
		} catch (Exception e) {
			snapshotSentThisLaunch.set(false);
			LOGGER.error("Failed to collect snapshot", e);
			return;
		}

		try {
			SENDER.execute(() -> {
				try {
					if (noUserConsent()) {
						snapshotSentThisLaunch.set(false);
						LOGGER.info("Snapshot send cancelled, user consent revoked before send");
						return;
					}
					boolean sent = openMCDBClient.send(snapshot);
					if (!sent) {
						snapshotSentThisLaunch.set(false);
						LOGGER.info("Snapshot send skipped, user consent revoked");
						return;
					}
					CONFIG.markFirstSent();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					snapshotSentThisLaunch.set(false);
					LOGGER.warn("Snapshot send interrupted", e);
				} catch (Exception e) {
					snapshotSentThisLaunch.set(false);
					LOGGER.error("Failed to send snapshot", e);
				}
			});
		} catch (Exception e) {
			snapshotSentThisLaunch.set(false);
			LOGGER.error("Failed to queue snapshot send", e);
		}
	}
}
