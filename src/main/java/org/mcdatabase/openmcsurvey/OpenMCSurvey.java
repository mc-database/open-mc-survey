package org.mcdatabase.openmcsurvey;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenMCSurvey implements ModInitializer {
	public static final String MOD_ID = "openmcsurvey";

	private static final String OPENMCDB_URL = "http://0.0.0.0:8000";
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

		final Snapshot snapshot;
		final boolean firstRequest = !CONFIG.hasSentFirst();
		try {
			snapshot = Surveyor.collect(firstRequest);
		} catch (Exception e) {
			LOGGER.error("Failed to collect snapshot", e);
			return;
		}

		SENDER.execute(() -> {
			try {
				openMCDBClient.send(snapshot);
				CONFIG.markFirstSent();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.warn("Snapshot send interrupted", e);
			} catch (Exception e) {
				LOGGER.error("Failed to send snapshot", e);
			}
		});
	}
}
