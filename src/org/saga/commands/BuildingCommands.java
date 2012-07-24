package org.saga.commands;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.saga.Saga;
import org.saga.SagaLogger;
import org.saga.buildings.Arena;
import org.saga.buildings.Building;
import org.saga.buildings.Home;
import org.saga.buildings.TownSquare;
import org.saga.buildings.TradingPost;
import org.saga.buildings.storage.StorageArea;
import org.saga.chunks.ChunkBundle;
import org.saga.chunks.ChunkBundleManager;
import org.saga.chunks.SagaChunk;
import org.saga.economy.EconomyManager;
import org.saga.economy.EconomyManager.InvalidWorldException;
import org.saga.economy.EconomyManager.TradeDealNotFoundException;
import org.saga.economy.TradeDeal;
import org.saga.messages.BuildingMessages;
import org.saga.messages.SettlementMessages;
import org.saga.messages.EconomyMessages;
import org.saga.messages.SagaMessages;
import org.saga.messages.SettlementEffects;
import org.saga.player.SagaPlayer;
import org.saga.settlements.Settlement.SettlementPermission;
import org.saga.utility.text.TextUtil;
import org.sk89q.Command;
import org.sk89q.CommandContext;
import org.sk89q.CommandPermissions;


public class BuildingCommands {

	
	// General building storage:
	@Command(
			aliases = {"baddstorage","baddstore"},
			usage = "",
			flags = "",
			desc = "Add a storage area to the building.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.user.building.storage.add"})
	public static void addStorage(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
	

		// Retrieve Saga chunk:
		SagaChunk selChunk = sagaPlayer.getSagaChunk();
		if(selChunk == null){
			sagaPlayer.message(SettlementMessages.chunkNotClaimed());
			return;
		}
		
		// Retrieve building:
		Building selBuilding = selChunk.getBuilding();
		if(selBuilding == null){
			sagaPlayer.message(BuildingMessages.noBuilding());
			return;
		}
	
		// Remaining storage areas:
		if(selBuilding.getRemainingStorageAreas() < 1){
			sagaPlayer.message(BuildingMessages.storeAreasUnavailable(selBuilding));
			return;
		}
		
		// Create storage:
		StorageArea newStoreArea = new StorageArea(sagaPlayer);
	
		// Check overlap:
		if(selBuilding.checkOverlap(newStoreArea)){
			sagaPlayer.message(BuildingMessages.storeAreaOverlap());
			return;
		}
		
		// Multiple chunks:
		ArrayList<SagaChunk> sagaChunks = newStoreArea.getSagaChunks();
		if(sagaChunks.size() > 1 || !sagaChunks.get(0).equals(selChunk)){
			sagaPlayer.message(BuildingMessages.storeAreaSingleChunk());
			return;
		}
		
		// Add:
		selBuilding.addStorageArea(newStoreArea);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.storeAreaAdded(selBuilding));
		
		// Effect:
		SettlementEffects.playStoreAreaCreate(sagaPlayer, newStoreArea);
		
		
	}
	
	@Command(
			aliases = {"bremovestorage","bremovestore"},
			usage = "",
			flags = "",
			desc = "Remove a storage area to the building.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.user.building.storage.remove"})
	public static void removeStorage(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
	

		// Retrieve Saga chunk:
		SagaChunk selChunk = sagaPlayer.getSagaChunk();
		if(selChunk == null){
			sagaPlayer.message(SettlementMessages.chunkNotClaimed());
			return;
		}
		
		// Retrieve building:
		Building selBuilding = selChunk.getBuilding();
		if(selBuilding == null){
			sagaPlayer.message(BuildingMessages.noBuilding());
			return;
		}
	
		// Retrieve storage:
		StorageArea storageArea = selBuilding.getStorageArea(sagaPlayer.getLocation());
		
		// No storage area:
		if(storageArea == null){
			sagaPlayer.message(BuildingMessages.storeAreaNotFound(selBuilding));
			return;
		}
		
		// Remove:
		selBuilding.removeStorageArea(storageArea);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.storeAreaRemoved(selBuilding));
		
		// Effect:
		SettlementEffects.playStoreAreaRemove(sagaPlayer, storageArea);
		
		
	}
	
	
	
	// Arena:
	@Command(
			aliases = {"btop"},
			usage = "<amount to displaye>",
			flags = "",
			desc = "Show top players for the arena.",
			min = 0,
			max = 1
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.setsell"})
	public static void top(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		Integer count = null;
		
		// Retrieve building:
		Arena selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, Arena.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
	
		// Arguments:
		if (args.argsLength() == 1) {
		
			try {
				count = Integer.parseInt(args.getString(0));
			} catch (NumberFormatException e) {
				sagaPlayer.message(SettlementMessages.invalidInteger(args.getString(0)));
				return;
			}
			
		}else{
			
			count = 10;
			
		}
		
		// Inform:
		sagaPlayer.message(BuildingMessages.arenaTop(selectedBuilding, count));
		
	
	}

	

	// Home:
	@Command(
			aliases = {"baddresident"},
			usage = "<name>",
			flags = "",
			desc = "Add a resident to a home.",
			min = 1,
			max = 1
	)
	@CommandPermissions({"saga.user.building.home.addresident"})
	public static void addResident(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		String targetName = null;
		
		// Retrieve building:
		Home selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, Home.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
	
		// Arguments:
		targetName = args.getString(0);

		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.hasPermission(sagaPlayer, SettlementPermission.ADD_RESIDENT)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Member:
		
		if(!SagaPlayer.checkExists(targetName)){
			sagaPlayer.message(SettlementMessages.nonExistantPlayer(targetName));
			return;
		}
		
		// Already a resident:
		if(selectedBuilding.isResident(targetName)){
			sagaPlayer.message(BuildingMessages.alreadyResident(targetName));
			return;
		}
		
		// Add:
		selectedBuilding.addResident(targetName);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.addedResident(targetName));
		
	
	}
	
	@Command(
			aliases = {"bremoveresident"},
			usage = "<name>",
			flags = "",
			desc = "Remove a resident from a home.",
			min = 1,
			max = 1
	)
	@CommandPermissions({"saga.user.building.home.removeresident"})
	public static void removeResident(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		String targetName = null;
		
		// Retrieve building:
		Home selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, Home.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
	
		// Arguments:
		targetName = args.getString(0);
		
		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.hasPermission(sagaPlayer, SettlementPermission.REMOVE_RESIDENT)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Already a resident:
		if(!selectedBuilding.isResident(targetName)){
			sagaPlayer.message(BuildingMessages.notResident(targetName));
			return;
		}
		
		// Remove:
		selectedBuilding.removeResident(targetName);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.removedResident(targetName));
		
	
	}
	
	
	
	// Trading post:
	@Command(
			aliases = {"bsetsell", "setsell"},
			usage = "[item] <price>",
			flags = "",
			desc = "Sets item sell price. Item in hand if no item is given.",
			min = 1,
			max = 2
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.setsell"})
	public static void setSell(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
		
		Material material = null;
		Double price = null;

		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.hasPermission(sagaPlayer, SettlementPermission.MANAGE_PRICES)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Arguments:
		if(args.argsLength() == 2){
			
			String sMaterial = args.getString(0);
			material = Material.matchMaterial(sMaterial);
			if(material == null){
				try {
					material = Material.getMaterial(Integer.parseInt(sMaterial));
				} catch (NumberFormatException e) { }
			}
			if(material == null){
				sagaPlayer.message(EconomyMessages.invalidMaterial(sMaterial));
				return;
			}
			
			// Price:
			String sValue = args.getString(1);
			try {
				price = Double.parseDouble(sValue);
			} catch (NumberFormatException e) {
				sagaPlayer.message(EconomyMessages.invalidPrice(sValue));
				return;
			}
			
		}else{
			
			// Material:
			ItemStack item = sagaPlayer.getItemInHand();
			if(item != null && item.getType() != Material.AIR) material = item.getType();
			if(material == null){
				sagaPlayer.message(EconomyMessages.invalidItemHand());
				return;
			}
			
			// Price:
			String sValue = args.getString(0);
			try {
				price = Double.parseDouble(sValue);
			} catch (NumberFormatException e) {
				sagaPlayer.message(EconomyMessages.invalidPrice(sValue));
				return;
			}
			
		}
		
		// Add price:
		selectedBuilding.setSellPrice(material, price);
		
		// Inform:
		sagaPlayer.message(EconomyMessages.setSell(material, price));
		
		// Notify transaction:
		selectedBuilding.notifyTransaction();
		
		
	}

	@Command(
			aliases = {"bremovesell", "removesell"},
			usage = "[item]",
			flags = "",
			desc = "Removes item sell price. Item in hand if no item is given.",
			min = 1,
			max = 0
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.removesell"})
	public static void removeSell(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
		
		Material material = null;

		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.hasPermission(sagaPlayer, SettlementPermission.MANAGE_PRICES)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Arguments:
		if(args.argsLength() == 1){
			
			String sMaterial = args.getString(0);
			material = Material.matchMaterial(sMaterial);
			if(material == null){
				try {
					material = Material.getMaterial(Integer.parseInt(sMaterial));
				} catch (NumberFormatException e) { }
			}
			if(material == null){
				sagaPlayer.message(EconomyMessages.invalidMaterial(sMaterial));
				return;
			}
			
		}else{
			
			// Material:
			ItemStack item = sagaPlayer.getItemInHand();
			if(item != null && item.getType() != Material.AIR) material = item.getType();
			if(material == null){
				sagaPlayer.message(EconomyMessages.invalidItemHand());
				return;
			}
			
		}
		
		// Remove price:
		selectedBuilding.removeSellPrice(material);
		
		// Inform:
		sagaPlayer.message(EconomyMessages.removeSell(material));
		
		// Notify transaction:
		selectedBuilding.notifyTransaction();
		
		
	}
	
	@Command(
			aliases = {"bsetbuy", "setbuy"},
			usage = "[item] <price>",
			flags = "",
			desc = "Sets item buy price. Item in hand if no item is given.",
			min = 1,
			max = 2
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.setsell"})
	public static void setBuy(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
		
		Material material = null;
		Double price = null;

		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.hasPermission(sagaPlayer, SettlementPermission.MANAGE_PRICES)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Arguments:
		if(args.argsLength() == 2){
			
			String sMaterial = args.getString(0);
			material = Material.matchMaterial(sMaterial);
			if(material == null){
				try {
					material = Material.getMaterial(Integer.parseInt(sMaterial));
				} catch (NumberFormatException e) { }
			}
			if(material == null){
				sagaPlayer.message(EconomyMessages.invalidMaterial(sMaterial));
				return;
			}
			
			// Price:
			String sValue = args.getString(1);
			try {
				price = Double.parseDouble(sValue);
			} catch (NumberFormatException e) {
				sagaPlayer.message(EconomyMessages.invalidPrice(sValue));
				return;
			}
			
		}else{
			
			// Material:
			ItemStack item = sagaPlayer.getItemInHand();
			if(item != null && item.getType() != Material.AIR) material = item.getType();
			if(material == null){
				sagaPlayer.message(EconomyMessages.invalidItemHand());
				return;
			}
			
			// Price:
			String sValue = args.getString(0);
			try {
				price = Double.parseDouble(sValue);
			} catch (NumberFormatException e) {
				sagaPlayer.message(EconomyMessages.invalidPrice(sValue));
				return;
			}
			
		}
		
		// Add price:
		selectedBuilding.setBuyPrice(material, price);
		
		// Inform:
		sagaPlayer.message(EconomyMessages.setBuy(material, price));
		
		// Notify transaction:
		selectedBuilding.notifyTransaction();
		
		
	}

	@Command(
			aliases = {"bremovebuy", "removebuy"},
			usage = "[item]",
			flags = "",
			desc = "Removes item buy price. Item in hand if no item is given.",
			min = 1,
			max = 0
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.removesell"})
	public static void removeBuy(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
		
		Material material = null;

		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.hasPermission(sagaPlayer, SettlementPermission.MANAGE_PRICES)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Arguments:
		if(args.argsLength() == 1){
			
			String sMaterial = args.getString(0);
			material = Material.matchMaterial(sMaterial);
			if(material == null){
				try {
					material = Material.getMaterial(Integer.parseInt(sMaterial));
				} catch (NumberFormatException e) { }
			}
			if(material == null){
				sagaPlayer.message(EconomyMessages.invalidMaterial(sMaterial));
				return;
			}
			
		}else{
			
			// Material:
			ItemStack item = sagaPlayer.getItemInHand();
			if(item != null && item.getType() != Material.AIR) material = item.getType();
			if(material == null){
				sagaPlayer.message(EconomyMessages.invalidItemHand());
				return;
			}
			
		}
		
		// Remove price:
		selectedBuilding.removeBuyPrice(material);
		
		// Inform:
		sagaPlayer.message(EconomyMessages.removeSell(material));
		
		// Notify transaction:
		selectedBuilding.notifyTransaction();
		
		
	}
	
	
	@Command(
			aliases = {"bdonate", "donate"},
			usage = "",
			flags = "",
			desc = "Donates an item held in hand to the tading post.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.donate"})
	public static void donate(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		

		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}

		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.isMember(sagaPlayer)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Item:
		ItemStack item = sagaPlayer.getItemInHand();
		if(item == null) return;

		// Damaged:
		if(item.getDurability() > 0){
			sagaPlayer.message(BuildingMessages.cantDonateDamaged());
			return;
		}
		
		// Enchanted:
		if(item.getEnchantments().size() > 0){
			sagaPlayer.message(BuildingMessages.cantDonateEnchanted());
			return;
		}
		
		// Remove:
		item = sagaPlayer.removeItemInHand();
		if(item.getType().equals(Material.AIR)){
			sagaPlayer.message(EconomyMessages.nothingInHand());
			return;
		}

		// Put:
		selectedBuilding.addItem(item);
		
		// Inform:
		SagaChunk sagaChunk = selectedBuilding.getSagaChunk();
		if(sagaChunk != null){
			sagaChunk.getChunkBundle().broadcast(EconomyMessages.donatedItemsBroadcast(item, selectedBuilding, sagaPlayer));
		}
		
		// Notify transaction:
		selectedBuilding.notifyTransaction();
		
		
	}
	
	@Command(
			aliases = {"bdonateall","donateall"},
			usage = "<item>",
			flags = "",
			desc = "Donates all items of the give type.",
			min = 1,
			max = 1
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.donate"})
	public static void donateAll(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		

		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}

		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.isMember(sagaPlayer)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Arguments:
		Material material = Material.matchMaterial(args.getString(0));
		if(material == null || material.equals(Material.AIR)){
			sagaPlayer.message( EconomyMessages.invalidMaterial(args.getString(0)) );
			return;
		}
		
		// Add items:
		int amount = 0;
		for (int i = 0; i < sagaPlayer.getInventorySize(); i++) {
			
			ItemStack item = sagaPlayer.getInventoryItem(i);
			
			if(item != null && item.getType().equals(material) && item.getDurability() == 0 && item.getEnchantments().size() == 0){

				amount += item.getAmount();
				selectedBuilding.addItem(item);
				sagaPlayer.removeInventoryItem(i);
				
			}
			
		}
		
		// Inform:
		SagaChunk sagaChunk = selectedBuilding.getSagaChunk();
		if(sagaChunk != null){
			sagaChunk.getChunkBundle().broadcast(EconomyMessages.donatedItemsBroadcast(new ItemStack(material, amount), selectedBuilding, sagaPlayer));
		}
		
		// Notify transactions:
		selectedBuilding.notifyTransaction();
		
	}
	
	@Command(
			aliases = {"bdonatecurrency", "donatec"},
			usage = "<amount>",
			flags = "",
			desc = "Donates currency to the tading post.",
			min = 1,
			max = 1
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.donate"})
	public static void donateCoins(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		

		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}

		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.isMember(sagaPlayer)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Amount:
		Double amount = null;
		try {
			amount = Double.parseDouble(args.getString(0));
		} catch (NumberFormatException e) {
			sagaPlayer.message(EconomyMessages.invalidAmount(args.getString(0)));
			return;
		}
		
		// Check if enough:
		if(sagaPlayer.getCoins() < amount){
			sagaPlayer.message(EconomyMessages.notEnoughCoins());
			return;
		}
		
		// Remove:
		sagaPlayer.removeCoins(amount);

		// Put:
		selectedBuilding.addCoins(amount);
		
		// Inform:
		SagaChunk sagaChunk = selectedBuilding.getSagaChunk();
		if(sagaChunk != null){
			sagaChunk.getChunkBundle().broadcast(EconomyMessages.donatedCurrencyBroadcast(amount, selectedBuilding, sagaPlayer));
		}

		// Notify transaction:
		selectedBuilding.notifyTransaction();
		
		
	}
	
	@Command(
			aliases = {"btradingpost", "tpost"},
			usage = "",
			flags = "",
			desc = "Information about the trading post building.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.user.building.tradingpost.put"})
	public static void tradingPost(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		

		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
		
		// Inform:
		sagaPlayer.message(EconomyMessages.tradingpost(selectedBuilding));
		
		
	}

	@Command(
			aliases = {"bnewdeal","bnewimport","bnewexport", "newdeal","newimport","newexport"},
			usage = "<trade deal ID>",
			flags = "",
			desc = "Form a new trading deal.",
			min = 1,
			max = 1
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.formdeal"})
	public static void newdeal(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		

		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}

		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.hasPermission(sagaPlayer, SettlementPermission.MANAGE_DEALS)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Retrieve manager:
		EconomyManager manager = null;
		try {
			manager = EconomyManager.manager(sagaPlayer.getLocation());
		} catch (InvalidWorldException e1) {
			SagaLogger.severe(EconomyCommands.class, "failed to retrieve " + EconomyManager.class.getSimpleName() + ": " + e1.getClass().getSimpleName() + ":" + e1.getMessage());
			sagaPlayer.error("failed to retrieve " + EconomyManager.class.getSimpleName());
		}
		
		// Check existing trade deals:
		if(selectedBuilding.getDealCount() >= selectedBuilding.getDealsMaxCount()){
			sagaPlayer.message(EconomyMessages.tradeDealLimitReached(selectedBuilding));
			return;
		}
		
		// Check ID:
		Integer id;
		try {
			id = Integer.parseInt(args.getString(0));
		} catch (NumberFormatException e) {
			sagaPlayer.message(EconomyMessages.invalidId(args.getString(0)));
			return;
		}
		
		// Retrieve trade deal:
		TradeDeal tradeDeal = null;
		try {
			tradeDeal = manager.takeTradeDeal(id);
		} catch (TradeDealNotFoundException e) {
			sagaPlayer.message(EconomyMessages.invalidId(args.getString(0)));
			return;
		}
		
		// Add trade deal:
		selectedBuilding.addDeal(tradeDeal);
		
		// Inform:
		if(selectedBuilding.getChunkBundle() != null){
			selectedBuilding.getChunkBundle().broadcast(BuildingMessages.formedDealBrdc(tradeDeal, sagaPlayer));
		}
		
		// Report:
		selectedBuilding.addNewDeal(tradeDeal);
		
		
	}

	@Command(
			aliases = {"bsetminimumcoins","bsetminc"},
			usage = "<amount>",
			flags = "",
			desc = "Sets the coins that can't be used for imports.",
			min = 1,
			max = 1
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.reserve"})
	public static void setMinCoins(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		Double amount = null;

		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}

		// Permission:
		ChunkBundle chunkBundle = selectedBuilding.getChunkBundle();
		if(!chunkBundle.hasPermission(sagaPlayer, SettlementPermission.MANAGE_DEALS)){
			sagaPlayer.message(SagaMessages.noPermission(chunkBundle));
			return;
		}
		
		// Arguments:
		try {
			amount = Double.parseDouble(args.getString(0));
		} catch (NumberFormatException e) {
			sagaPlayer.message(EconomyMessages.invalidAmount(args.getString(0)));
			return;
		}
		
		// Reserve:
		selectedBuilding.setMinimumCoins(amount);
		
		// Inform:
		if(selectedBuilding.getChunkBundle() != null){
			selectedBuilding.getChunkBundle().broadcast(EconomyMessages.reservedBroadcast(amount, sagaPlayer, selectedBuilding));
		}
		
		
	}
	

	@Command(
			aliases = {"atpostautomatic","asetautomatic"},
			usage = "",
			flags = "",
			desc = "Sets the trading post to automatic.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.admin.economy.tradingpost.automatic"})
	public static void setAutomatic(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
		
		// Already automatic:
		if(selectedBuilding.isAutomated()){
			sagaPlayer.message(BuildingMessages.alreadyAutomatic(selectedBuilding));
			return;
		}
		
		// Set automatic:
		selectedBuilding.setAutomated(true);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.setAutomate(selectedBuilding));
		
		
	}
	
	@Command(
			aliases = {"atpostmanual","asetmanual"},
			usage = "",
			flags = "",
			desc = "Sets the trading post to manual.",
			min = 0,
			max = 0
	)
	@CommandPermissions({"saga.admin.economy.tradingpost.automatic"})
	public static void setManual(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {
		
		
		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
		
		// Already manual:
		if(!selectedBuilding.isAutomated()){
			sagaPlayer.message(BuildingMessages.alreadyAutomatic(selectedBuilding));
			return;
		}
		
		// Set manual:
		selectedBuilding.setAutomated(false);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.setAutomate(selectedBuilding));
		
		
	}
	
	
	@Command(
			aliases = {"bgoods","goods"},
			usage = "",
			flags = "",
			desc = "Shows trading post goods.",
			min = 0,
			max = 1
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.goods"})
	public static void levels(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {


		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
		
    	// Arguments:
		Integer page = null;
    	if(args.argsLength() == 1){
    		
        	try {
    			page = Integer.parseInt(args.getString(0));
    		} catch (NumberFormatException e) {
    			sagaPlayer.message(SettlementMessages.invalidPage(args.getString(0)));
    			return;
    		}
    		
    	}else{
    		
    		page = 1;
        	
    	}
		
	    // Inform:
	    sagaPlayer.message(BuildingMessages.goods(selectedBuilding, page - 1));
	      
	    
	}
	
	@Command(
			aliases = {"breport","report"},
			usage = "",
			flags = "",
			desc = "Shows trading post report.",
			min = 0,
			max = 1
	)
	@CommandPermissions({"saga.user.building.tradingpost.economy.report"})
	public static void report(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {


		// Retrieve building:
		TradingPost selectedBuilding = null;
		try {
			selectedBuilding = Building.retrieveBuilding(args, plugin, sagaPlayer, TradingPost.class);
		} catch (Throwable e) {
			sagaPlayer.message(e.getMessage());
			return;
		}
		
    	// Arguments:
		Integer page = null;
    	if(args.argsLength() == 1){
    		
        	try {
    			page = Integer.parseInt(args.getString(0));
    		} catch (NumberFormatException e) {
    			sagaPlayer.message(SettlementMessages.invalidPage(args.getString(0)));
    			return;
    		}
    		
    	}else{
    		
    		page = 1;
        	
    	}
		
	    // Inform:
	    sagaPlayer.message(BuildingMessages.report(selectedBuilding, page - 1));
	      
	    
	}
	
	
	
	// Town square:
	@Command(
            aliases = {"sspawn"},
            usage = "",
            flags = "",
            desc = "Spawn in your settlement town square.",
            min = 0,
            max = 1)
	@CommandPermissions({"saga.user.settlement.building.townsquare.spawn"})
	public static void spawn(CommandContext args, Saga plugin, SagaPlayer sagaPlayer) {

		
		ChunkBundle selectedChunkBundle = null;
		
		// Arguments:
		if(args.argsLength() == 1){
			
			// Chunk group:
			String groupName = args.getString(0).replaceAll(SagaMessages.spaceSymbol, " ");
			selectedChunkBundle = ChunkBundleManager.manager().getChunkBundleWithName(groupName);
			if(selectedChunkBundle == null){
				sagaPlayer.message(SettlementMessages.noChunkGroup(groupName));
				return;
			}
			
		}else{
			
			// Chunk group:
			selectedChunkBundle = sagaPlayer.getChunkBundle();
			if(selectedChunkBundle == null){
				sagaPlayer.message( SettlementMessages.noChunkGroup() );
				return;
			}
			
		}
		
		// Permission:
		if( !selectedChunkBundle.hasPermission(sagaPlayer, SettlementPermission.SPAWN) ){
			sagaPlayer.message(SagaMessages.noPermission());
			return;
		}
		
		ArrayList<TownSquare> selectedBuildings = selectedChunkBundle.getBuildings(TownSquare.class);
		
		if(selectedBuildings.size() == 0){
			sagaPlayer.message(BuildingMessages.noTownSquare(selectedChunkBundle));
			return;
		}
		
		Integer smallestCooldown = Integer.MAX_VALUE;
		TownSquare selectedBuilding = null;
		
		for (TownSquare townSquare : selectedBuildings) {
			
			selectedBuilding = townSquare;
			break;
			
		}
		
		// Everything on cool down:
		if(selectedBuilding == null){
			sagaPlayer.message(BuildingMessages.cooldown(TextUtil.className(TownSquare.class), smallestCooldown));
			return;
		}
		
		// Prepare chunk:
		selectedBuilding.getSagaChunk().loadChunk();
		
		// Location:
		Location spawnLocation = selectedBuilding.getSpawnLocation();
		if(spawnLocation == null){
			SagaLogger.severe(selectedBuilding, sagaPlayer + " player failed to respawn at " + selectedBuilding.getDisplayName());
			sagaPlayer.error("failed to respawn");
			return;
		}
		
		// Teleport:
		sagaPlayer.teleport(spawnLocation);
		
	
	}
	
	
}
