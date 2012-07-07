package org.saga.buildings.signs;

import org.bukkit.block.Sign;
import org.saga.buildings.Building;
import org.saga.config.AttributeConfiguration;
import org.saga.messages.AbilityEffects;
import org.saga.messages.BuildingMessages;
import org.saga.player.SagaPlayer;


public class AttributeSign extends BuildingSign{

	
	/**
	 * Name for the 
	 */
	public static String SIGN_NAME = "=[INCREASE]=";
	
	
	// Initialisation:
	/**
	 * Creates a stone 
	 * 
	 * @param type transaction type
	 * @param sign sign
	 * @param secondLine second line
	 * @param thirdLine third line
	 * @param fourthLine fourth line
	 * @param event event that created the sign
	 * @param building building
	 */
	private AttributeSign(Sign sign, String secondLine, String thirdLine, String fourthLine, Building building){
	
		super(sign, SIGN_NAME, secondLine, thirdLine, fourthLine, building);
		
	}
	
	/**
	 * Creates the training 
	 * 
	 * @param sign bukkit sign
	 * @param firstLine first line
	 * @param secondLine second line
	 * @param thirdLine third line
	 * @param fourthLine fourth line
	 * @param building building
	 * @return training sign
	 */
	public static AttributeSign create(Sign sign, String secondLine, String thirdLine, String fourthLine, Building building) {
		return new AttributeSign(sign, secondLine, thirdLine, fourthLine, building);
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.signs.BuildingSign#getName()
	 */
	@Override
	public String getName() {
		return SIGN_NAME;
	}

	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.signs.BuildingSign#getStatus()
	 */
	@Override
	public SignStatus getStatus() {
	
		
		String attribute = getFirstParameter();
		
		if(!getBuilding().getDefinition().hasAttribute(attribute)) return SignStatus.INVALIDATED;

		return SignStatus.ENABLED;
		
		
	}
	
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.signs.BuildingSign#getLine(int, org.saga.buildings.signs.BuildingSign.SignStatus)
	 */
	@Override
	public String getLine(int index, SignStatus status) {
	
		
		switch (status) {
			
			case ENABLED:

				if(index == 1) return getFirstParameter();
				break;
				
			case DISABLED:

				if(index == 1) return getFirstParameter();
				break;
				
			default:
				
				if(index == 1) return "-";
				break;

		}

		return "";
		
		
	}
	
	
	// Events:
	/* 
	 * (non-Javadoc)
	 * 
	 * @see org.saga.buildings.signs.BuildingSign#onRightClick(org.saga.player.SagaPlayer)
	 */
	@Override
	protected void onRightClick(SagaPlayer sagaPlayer) {

		String attribute = getFirstParameter();
		Integer attributeScore = sagaPlayer.getAttributeScore(attribute) + 1;
		
		// Already maximum:
		if(attributeScore > AttributeConfiguration.config().maxAttributeScore){
			sagaPlayer.message(BuildingMessages.attributeMaxReached(attribute));
			return;
		}
		
		// Available points:
		if(sagaPlayer.getRemainingAttributePoints() < 1){
			sagaPlayer.message(BuildingMessages.attributePointsRequired(attribute));
			return;
		}
		
		// Increase:
		sagaPlayer.setAttributeScore(attribute, attributeScore);
		
		// Inform:
		sagaPlayer.message(BuildingMessages.attributeIncreased(attribute, attributeScore));
		
		// Play effect:
		AbilityEffects.playSign(sagaPlayer);
		
		
	}
	
	
}