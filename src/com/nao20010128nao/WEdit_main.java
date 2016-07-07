package com.nao20010128nao;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.nao20010128nao.WEdit_main.SessionData.UndoEntries;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

public class WEdit_main extends PluginBase implements Listener {
	Config settings;
	int id;
	boolean async;
	Map<String, SessionData> sessions;

	@Override
	public void onLoad() {
		// TODO 自動生成されたメソッド・スタブ
		getLogger().info("WEditが読み込まれました。" + TextFormat.GREEN + "(v2.0.0 - by Madness LesMi)");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		// TODO 自動生成されたメソッド・スタブ
		File dataFolder = getDataFolder();
		if (!dataFolder.exists())
			dataFolder.mkdirs();
		settings = new Config(new File(dataFolder, "settings.yml"), Config.YAML, new LinkedHashMap<String, Object>() {
			{
				put("block_id", 155);
				put("async", true);
			}
		});
		id = settings.getInt("block_id");
		async = settings.getBoolean("async");
		getServer().getPluginManager().registerEvents(this, this);
		sessions = new HashMap<String, SessionData>() {
			@Override
			public SessionData put(String key, SessionData value) {
				// TODO 自動生成されたメソッド・スタブ
				return super.put(key.toLowerCase(), value);
			}

			@Override
			public SessionData get(Object key) {
				// TODO 自動生成されたメソッド・スタブ
				return super.get(((String) key).toLowerCase());
			}

			@Override
			public boolean containsKey(Object key) {
				// TODO 自動生成されたメソッド・スタブ
				return super.containsKey(((String) key).toLowerCase());
			}

			@Override
			public SessionData remove(Object key) {
				// TODO 自動生成されたメソッド・スタブ
				return super.remove(((String) key).toLowerCase());
			}
		};
	}

	@EventHandler
	public void BlockBreak(BlockBreakEvent event) {
		int id = event.getItem().getId();
		if (id == this.id) {
			Player player = event.getPlayer();
			String user = player.getName();

			if (!sessions.containsKey(user))
				sessions.put(user, new SessionData());
			SessionData session = sessions.get(user);
			if (session.pos1 == null) {
				int x = (int) Math.floor(event.getBlock().x);
				int y = (int) Math.floor(event.getBlock().y);
				int z = (int) Math.floor(event.getBlock().z);
				session.pos1 = new Vector3(x, y, z);
				String ms = "[WEdit] POS1が設定されました。: " + x + ", " + y + ", " + z;
				if (session.pos2 != null) {
					int num = countBlocks(player);
					if (num != 1)
						ms += " (計" + num + "ブロック)";
				}
				player.sendMessage(ms);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void Place(BlockPlaceEvent event) {
		int id = event.getItem().getId();
		if (id == this.id) {
			Player player = event.getPlayer();
			String user = player.getName();

			if (!sessions.containsKey(user))
				sessions.put(user, new SessionData());
			SessionData session = sessions.get(user);
			if (session.pos2 == null) {
				int x = (int) Math.floor(event.getBlock().x);
				int y = (int) Math.floor(event.getBlock().y);
				int z = (int) Math.floor(event.getBlock().z);
				session.pos2 = new Vector3(x, y, z);
				String ms = "[WEdit] POS2が設定されました。: " + x + ", " + y + ", " + z;
				if (session.pos1 != null) {
					int num = countBlocks(player);
					if (num != 1)
						ms += " (計" + num + "ブロック)";
				}
				player.sendMessage(ms);
				event.setCancelled(true);
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		String user = sender.getName();
		if (sender.getName().equalsIgnoreCase("console")) {
			sender.sendMessage("[WEdit] WEditはゲーム内でのみご利用いただけます。");
			return true;
		}
		switch (command.getName()) {
			case "/":
			case "/help":
				StringBuilder ms = new StringBuilder();
				ms.append("=== WEditの使い方 ===\n");
				ms.append("* ID:").append(id).append("を持ち、ブロックを破壊することでPOS1を設定\n");
				ms.append("* ID:").append(id).append("を設置することでPOS2を設定\n");
				ms.append("* POS1とPOS2を設定し終えたら、以下のコマンドを実行しましょう\n");
				ms.append("* //set <id> :範囲をブロックで埋めます\n");
				ms.append("* //cut :範囲のブロックを消します\n");
				ms.append("* //replace <id1> <id2> :id1をid2のブロックに置き換えます\n");
				ms.append("* //air <id> :範囲内のidのブロックのみ削除します\n");
				ms.append("* //e :設定されたPOS1とPOS2を削除します");
				sender.sendMessage(ms.toString());
				break;
			case "/copy":
				// copy(sender);
				break;
			case "/e":
				if (args.length == 0)
					erase((Player) sender);
				else
					erase((Player) sender, Integer.valueOf(args[0]));
				break;
			case "/set":
				if (args.length != 1)
					return false;
				set((Player) sender, args[0]);
				break;
			case "/replace":
				if (args.length != 2)
					return false;
				replace((Player) sender, args[0], args[1]);
				break;
			case "/air":
				if (args.length != 1)
					return false;
				replace((Player) sender, args[0], "0");
				break;
			case "/cut":
				set((Player) sender);
				break;
			case "/undo":
				undo((Player) sender);
				break;
			case "/pos":
				if (args.length != 4)
					return false;
				int num = Integer.valueOf(args[0]);
				Vector3 vec;
				switch (num) {
					case 1:
						vec = sessions.get(user).pos1;
						break;
					case 2:
						vec = sessions.get(user).pos2;
						break;
					default:
						sender.sendMessage("不正なパラメータのため、座標指定できません。");
						return true;
				}
				if (vec == null) {
					int x = Integer.valueOf(args[1]);
					int y = Integer.valueOf(args[2]);
					int z = Integer.valueOf(args[3]);
					switch (num) {
						case 1:
							sessions.get(user).pos1 = new Vector3(x, y, z);
							break;
						case 2:
							sessions.get(user).pos2 = new Vector3(x, y, z);
							break;
						default:
							sender.sendMessage("不正なパラメータのため、座標指定できません。");
							return true;
					}
					String ms1 = "[WEdit] POS" + num + "が設定されました。: " + x + ", " + y + ", " + z;
					switch (num) {
						case 1:
							if (sessions.get(user).pos2 != null) {
								num = countBlocks(sender);
								if (num != -1)
									ms1 += " (計" + num + "ブロック)";
							}
							break;
						case 2:
							if (sessions.get(user).pos1 != null) {
								num = countBlocks(sender);
								if (num != -1)
									ms1 += " (計" + num + "ブロック)";
							}
							break;
						default:
							sender.sendMessage("不正なパラメータのため、座標指定できません。");
							return true;
					}
					sender.sendMessage(ms1);
				} else
					sender.sendMessage("不正なパラメータのため、座標指定できません。");
				break;
		}
		return true;
	}

	public void set(Player player) {
		set(player, "0");
	}

	public void set(Player player, String id) {
		String name = player.getName();
		if (sessions.containsKey(name) && sessions.get(name).pos1 != null & sessions.get(name).pos2 != null) {
			SessionData pos = sessions.get(name);
			int sx = (int) Math.min(pos.pos1.x, pos.pos2.x);
			int sy = (int) Math.min(pos.pos1.y, pos.pos2.y);
			int sz = (int) Math.min(pos.pos1.z, pos.pos2.z);
			int ex = (int) Math.max(pos.pos1.x, pos.pos2.x);
			int ey = (int) Math.max(pos.pos1.y, pos.pos2.y);
			int ez = (int) Math.max(pos.pos1.z, pos.pos2.z);
			int num = (ex - sx + 1) * (ey - sy + 1) * (ez - sz + 1);
			if (id.equals("0") | id.startsWith("0:"))
				getServer().broadcastMessage("[WEdit] " + name + "が変更を開始します…(cut : " + num + "ブロック)");
			else
				getServer().broadcastMessage("[WEdit] " + name + "が変更を開始します…(set " + id + " : " + num + "ブロック)");
			Level level = player.getLevel();
			String[] did = id.split(Pattern.quote(":"));
			Block block;
			if (did.length != 2)
				block = Block.get(Integer.valueOf(did[0]));
			else
				block = Block.get(Integer.valueOf(did[0]), Integer.valueOf(did[1]));
			Thread t = new Thread(() -> {
				List<UndoEntries> data = new ArrayList<>();
				for (int x = sx; x <= ex; x++)
					for (int y = sy; y <= ey; y++)
						for (int z = sz; z <= ez; z++) {
							UndoEntries ue = new UndoEntries();
							ue.pos = new Vector3(x, y, z);
							ue.id = level.getBlockIdAt(x, y, z);
							ue.damage = level.getBlockDataAt(x, y, z);
							level.setBlock(ue.pos, block);
						}
				sessions.get(name).undo = data;
				getServer().broadcastMessage("[WEdit] 変更が終了しました。");
			});
			if (async)
				t.start();
			else
				t.run();
		} else
			player.sendMessage("[WEdit] ERROR: POS1とPOS2が指定されていません。\n[WEdit] //helpを打ち、使い方を読んでください。");
	}

	public void replace(Player player, String id1, String id2) {
		String name = player.getName();
		if (sessions.containsKey(name) && sessions.get(name).pos1 != null & sessions.get(name).pos2 != null) {
			SessionData pos = sessions.get(name);
			int sx = (int) Math.min(pos.pos1.x, pos.pos2.x);
			int sy = (int) Math.min(pos.pos1.y, pos.pos2.y);
			int sz = (int) Math.min(pos.pos1.z, pos.pos2.z);
			int ex = (int) Math.max(pos.pos1.x, pos.pos2.x);
			int ey = (int) Math.max(pos.pos1.y, pos.pos2.y);
			int ez = (int) Math.max(pos.pos1.z, pos.pos2.z);
			int num = (ex - sx + 1) * (ey - sy + 1) * (ez - sz + 1);
			getServer()
					.broadcastMessage("[WEdit] " + name + "が変更を開始します…(" + id1 + " => " + id2 + ") : " + num + "ブロック)");
			Level level = player.getLevel();
			String[] did = id2.split(Pattern.quote(":"));
			Block block;
			if (did.length != 2)
				block = Block.get(Integer.valueOf(did[0]));
			else
				block = Block.get(Integer.valueOf(did[0]), Integer.valueOf(did[1]));

			int id1id;
			int id1damage;
			did = id1.split(Pattern.quote(":"));
			id1id = Integer.valueOf(did[0]);
			if (did.length == 2)
				id1damage = Integer.valueOf(did[1]);
			else
				id1damage = 0;

			Thread t = new Thread(() -> {
				List<UndoEntries> data = new ArrayList<>();
				for (int x = sx; x <= ex; x++)
					for (int y = sy; y <= ey; y++)
						for (int z = sz; z <= ez; z++)
							if (level.getBlockIdAt(x, y, z) == id1id & level.getBlockDataAt(x, y, z) == id1damage) {
								UndoEntries ue = new UndoEntries();
								ue.pos = new Vector3(x, y, z);
								ue.id = level.getBlockIdAt(x, y, z);
								ue.damage = level.getBlockDataAt(x, y, z);
								level.setBlock(ue.pos, block);
							}
				sessions.get(name).undo = data;
				getServer().broadcastMessage("[WEdit] 変更が終了しました。");
			});
			if (async)
				t.start();
			else
				t.run();
		} else
			player.sendMessage("[WEdit] ERROR: POS1とPOS2が指定されていません。\n[WEdit] //helpを打ち、使い方を読んでください。");
	}

	public void undo(Player player) {
		String name = player.getName();
		if (sessions.containsKey(name) && sessions.get(name).undo != null && sessions.get(name).undo.size() != 0) {
			List<UndoEntries> data = sessions.get(name).undo;
			int num = data.size();
			getServer().broadcastMessage("[WEdit] " + name + "が変更を開始します…(undo : " + num + "ブロック)");
			Level level = player.getLevel();
			for (UndoEntries b : data) {
				Block block = Block.get(b.id, b.damage);
				Vector3 posi = b.pos;
				level.setBlock(posi, block);
			}
			sessions.get(name).undo = null;
			getServer().broadcastMessage("[WEdit] 変更が終了しました。");
		} else
			getServer().broadcastMessage("[WEdit] ERROR: やり直し出来ません。");
	}

	public void erase(Player player) {
		erase(player, 0);
	}

	public void erase(Player player, int t) {
		String name = player.getName();
		String ms = "[WEdit] " + t + "は不正な値です。";
		if (sessions.containsKey(name))
			switch (t) {
				case 0:
					sessions.remove(name);
					ms = "[WEdit] 座標データは削除されました。";
					break;
				case 1:
					if (sessions.get(name).pos1 != null) {
						sessions.get(name).pos1 = null;
						ms = "[WEdit] POS1は削除されました。";
					} else
						ms = "[WEdit] POS1は設定されていません。";
					break;
				case 2:
					if (sessions.get(name).pos2 != null) {
						sessions.get(name).pos2 = null;
						ms = "[WEdit] POS2は削除されました。";
					} else
						ms = "[WEdit] POS2は設定されていません。";
					break;
			}
		else
			ms = "[WEdit] POS1もPOS2も設定されていません。";
		player.sendMessage(ms);
	}

	public int countBlocks(CommandSender player) {
		String name;
		if (player == null)
			name = "console";
		else
			name = player.getName();
		if (sessions.containsKey(name) && sessions.get(name).pos1 != null & sessions.get(name).pos2 != null) {
			SessionData pos = sessions.get(name);
			int sx = (int) Math.min(pos.pos1.x, pos.pos2.x);
			int sy = (int) Math.min(pos.pos1.y, pos.pos2.y);
			int sz = (int) Math.min(pos.pos1.z, pos.pos2.z);
			int ex = (int) Math.max(pos.pos1.x, pos.pos2.x);
			int ey = (int) Math.max(pos.pos1.y, pos.pos2.y);
			int ez = (int) Math.max(pos.pos1.z, pos.pos2.z);
			int num = (ex - sx + 1) * (ey - sy + 1) * (ez - sz + 1);
			if (num < 0)
				num *= -1;
			return num;
		} else
			return -1;
	}

	public static class SessionData {
		public Vector3 pos1 = null, pos2 = null;
		public List<UndoEntries> undo;

		public static class UndoEntries {
			public Vector3 pos;
			public int id, damage;
		}
	}
}
