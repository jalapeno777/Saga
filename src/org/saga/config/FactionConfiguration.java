package org.saga.config;

import java.io.IOException;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonParseException;
import org.saga.SagaLogger;
import org.saga.factions.FactionDefinition;
import org.saga.saveload.Directory;
import org.saga.saveload.WriterReader;
import org.saga.utility.TwoPointFunction;

public class FactionConfiguration {


	/**
	 * Instance of the configuration.
	 */
	transient private static FactionConfiguration instance;
	
	/**
	 * Gets the instance.
	 * 
	 * @return instance
	 */
	public static FactionConfiguration config() {
		return instance;
	}
	
	
	/**
	 * Faction definition.
	 */
	private FactionDefinition definition;

	/**
	 * If true then only faction vs faction pvp is allowed.
	 */
	public Boolean factionOnlyPvp;
	
	
	// Creation:
	/**
	 * Amount of members needed for the faction to form.
	 */
	public Integer formationAmount;

	/**
	 * Minimum name length.
	 */
	private Integer minNameLength;

	/**
	 * Maximum name length.
	 */
	private Integer maxNameLength;
	
	
	
	
	// Claiming:
	/**
	 * Chunk bundle claim speed.
	 */
	private TwoPointFunction claimSpeed;

	/**
	 * Chunk bundle automatic unclaim speed.
	 */
	private TwoPointFunction unclaimSpeed;

	/**
	 * Claim speed multiplier for multiple members.
	 */
	private TwoPointFunction memberSpeedMult;
	
	/**
	 * Members required from both factions to be able to claim.
	 */
	private Integer toClaimMembers;

	/**
	 * Faction claim limit for given level.
	 */
	private TwoPointFunction claimLimit;

	/**
	 * Determines how much claims a settlement is worth.
	 */
	private TwoPointFunction claimPoints;
	
	/**
	 * Determines if claimed storage areas are considered open.
	 */
	private Boolean openClaimedStorageAreas;
	
	
	// Initialisation:
	/**
	 * Fixes all problematic fields.
	 * 
	 */
	public void complete() {
		
		
		if(definition == null){
			SagaLogger.nullField(getClass(), "definition");
			definition = FactionDefinition.defaultDefinition();
		}
		definition.complete();
		
		if(factionOnlyPvp == null){
			SagaLogger.nullField(getClass(), "factionOnlyPvp");
			factionOnlyPvp = true;
		}
		
		if(formationAmount == null){
			SagaLogger.nullField(getClass(), "formationAmount");
			formationAmount = 3;
		}
		
		if(minNameLength == null){
			SagaLogger.nullField(getClass(), "minNameLength");
			minNameLength = 1;
		}
		
		if(maxNameLength == null){
			SagaLogger.nullField(getClass(), "maxNameLength");
			maxNameLength = 5;
		}
		
		if(definition == null){
			SagaLogger.nullField(getClass(), "definition");
			definition = FactionDefinition.defaultDefinition();
		}

		if(claimSpeed == null){
			SagaLogger.nullField(getClass(), "claimSpeed");
			claimSpeed = new TwoPointFunction(0.0);
		}
		claimSpeed.complete();

		if(unclaimSpeed == null){
			SagaLogger.nullField(getClass(), "unclaimSpeed");
			unclaimSpeed = new TwoPointFunction(0.0);
		}
		unclaimSpeed.complete();

		if(memberSpeedMult == null){
			SagaLogger.nullField(getClass(), "memberSpeedMult");
			memberSpeedMult = new TwoPointFunction(0.0);
		}
		memberSpeedMult.complete();

		if(toClaimMembers == null){
			SagaLogger.nullField(getClass(), "toClaimMembers");
			toClaimMembers = 3;
		}
		
		if(claimLimit == null){
			SagaLogger.nullField(getClass(), "claimLimit");
			claimLimit = new TwoPointFunction(0.0);
		}
		claimLimit.complete();
		
		if(claimPoints == null){
			SagaLogger.nullField(getClass(), "claimPoints");
			claimPoints = new TwoPointFunction(0.0);
		}
		claimPoints.complete();

		if(openClaimedStorageAreas == null){
			SagaLogger.nullField(getClass(), "openClaimedStorageAreas");
			openClaimedStorageAreas = false;
		}
		
		
	}

	
	
	// Definition:
	/**
	 * Gets faction definition.
	 * 
	 * @return faction definition
	 */
	public FactionDefinition getDefinition() {
		return definition;
	}
	
	
	
	// Creation:
	/**
	 * Gets the minimum name length.
	 * 
	 * @return minimum name length
	 */
	public Integer getMinNameLength() {
		return minNameLength;
	}
	
	/**
	 * Gets the maximum name length.
	 * 
	 * @return maximum name length
	 */
	public Integer getMaxNameLength() {
		return maxNameLength;
	}
	
	
	
	// Faction claiming:
	/**
	 * Gets claim speed.
	 * 
	 * @param bundleLevel chunk bundle level
	 * @return claim speed
	 */
	public Double getClaimSpeed(Integer bundleLevel) {
		return claimSpeed.value(bundleLevel);
	}

	/**
	 * Gets unclaim speed.
	 * 
	 * @param bundleLevel chunk bundle level
	 * @return unclaim speed
	 */
	public Double getUnclaimSpeed(Integer bundleLevel) {
		return unclaimSpeed.value(bundleLevel);
	}
	

	/**
	 * Gets member bonus speed.
	 * 
	 * @param memberCount amount of members
	 * @return member bonus speed
	 */
	public Double getMemberMultiplier(Integer memberCount) {
		return memberSpeedMult.value(memberCount);
	}
	
	/**
	 * Gets the members required from both factions to be able to claim settlements.
	 * 
	 * @return members required to claim
	 */
	public Integer getToClaimMembers() {
		return toClaimMembers;
	}
	
	/**
	 * Gets claim limit.
	 * 
	 * @param level faction level
	 * @return claim limit
	 */
	public Integer getClaimLimit(Integer level) {
		return claimLimit.intValue(level);
	}
	
	/**
	 * Gets the amount claim points the settlement is worth.
	 * 
	 * @param settleLevel settlement level
	 * @return claim points
	 */
	public Double getClaimPoints(Integer settleLevel) {
		return claimPoints.value(settleLevel);
	}
	
	/**
	 * Checks if claimed storage areas are open for factions.
	 * 
	 * @return true if open
	 */
	public Boolean isOpenClaimedStorageAreas() {
		return openClaimedStorageAreas;
	}
	
	
	
	// Load unload:
	/**
	 * Loads configuration.
	 * 
	 * @return configuration
	 */
	public static FactionConfiguration load(){


		// Create config:
		if(!WriterReader.checkExists(Directory.FACTION_CONFIG)){

			try {
				WriterReader.unpackConfig(Directory.FACTION_CONFIG);
			}
			catch (IOException e) {
				SagaLogger.severe(FactionConfiguration.class, "failed to create default configuration: " + e.getClass().getSimpleName());
			}
			
		}
		
		FactionConfiguration config;
		try {
			
			config = WriterReader.read(Directory.FACTION_CONFIG, FactionConfiguration.class);
			
		} catch (IOException e) {
			
			SagaLogger.severe(FactionConfiguration.class, "failed to read configuration: " + e.getClass().getSimpleName());
			config = new FactionConfiguration();
			
		} catch (JsonParseException e) {

			SagaLogger.severe(FactionConfiguration.class, "failed to parse configuration: " + e.getClass().getSimpleName());
			SagaLogger.info("message: " + e.getMessage());
			config = new FactionConfiguration();
			
		}
		
		// Set instance:
		instance = config;
		
		config.complete();
		
		return config;
		
		
	}
	
	/**
	 * Unloads the instance.
	 * 
	 */
	public static void unload(){
		instance = null;
	}
	
	
}
