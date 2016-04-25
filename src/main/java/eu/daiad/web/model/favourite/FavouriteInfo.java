package eu.daiad.web.model.favourite;

import java.util.UUID;

import eu.daiad.web.domain.application.Favourite;
import eu.daiad.web.domain.application.FavouriteAccount;
import eu.daiad.web.domain.application.FavouriteGroup;



public class FavouriteInfo {
	
	private UUID refId;

	private String name;

	private EnumFavouriteType type;

	private long additionDateMils;
	
	public FavouriteInfo (Favourite favourite) {
		this.name = favourite.getLabel();		
		this.type = favourite.getType();
		this.additionDateMils = favourite.getCreatedOn().getMillis();
		switch (favourite.getType()){
		
		case ACCOUNT :
			FavouriteAccount accountFavourite = (FavouriteAccount) favourite;
			this.refId = accountFavourite.getAccount().getKey();
			break;
			
		case GROUP :
			FavouriteGroup groupFavourite = (FavouriteGroup) favourite;
			this.refId = groupFavourite.getGroup().getKey();
			break;
		
		default:
			this.refId = favourite.getKey();
		} 
	}

	public UUID getRefId() {
		return refId;
	}

	public String getName() {
		return name;
	}

	public EnumFavouriteType getType() {
		return type;
	}

	public long getAdditionDateMils() {
		return additionDateMils;
	}
}
