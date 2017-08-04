package one.lindegaard.MobHunting.storage.asynch;

import one.lindegaard.MobHunting.ConfigManager;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.storage.DataStoreException;
import one.lindegaard.MobHunting.storage.IDataStore;
import one.lindegaard.MobHunting.storage.PlayerSettings;
import one.lindegaard.MobHunting.storage.UserNotFoundException;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.HashSet;

public class PlayerSettingsRetrieverTask implements IDataStoreTask<PlayerSettings> {

	private OfflinePlayer mPlayer;
	private HashSet<Object> mWaiting;

	private Messages messages;
	private ConfigManager configManager;

	public PlayerSettingsRetrieverTask(OfflinePlayer player, HashSet<Object> waiting, Messages messages, ConfigManager configManager) {
		mPlayer = player;
		mWaiting = waiting;
		this.messages = messages;
		this.configManager = configManager;
	}

	//private void updateUsingCache(Set<PlayerSettings> achievements) {
	//	for (Object obj : mWaiting) {
	//		if (obj instanceof PlayerSettings) {
	//			PlayerSettings cached = (PlayerSettings) obj;
	//			if (!cached.getPlayer().equals(mPlayer))
	//				continue;
    //
	//		}
	//	}
	//}

	public PlayerSettings run(IDataStore store) throws DataStoreException {
		synchronized (mWaiting) {
			try {
				return store.loadPlayerSettings(mPlayer);
			} catch (UserNotFoundException e) {
				messages.debug("Saving new PlayerSettings for %s to database.", mPlayer.getName());
				PlayerSettings ps = new PlayerSettings(mPlayer, configManager.learningMode, false);
				try {
					store.insertPlayerSettings(ps);
				} catch (DataStoreException e1) {
					e1.printStackTrace();
				}
				return ps;
			} catch (DataStoreException e) {
				e.printStackTrace();
				return null;
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	@Override
	public boolean readOnly() {
		return true;
	}
}
