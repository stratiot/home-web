package eu.daiad.web.domain.application;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.daiad.web.model.favourite.EnumFavouriteType;

@Entity(name = "favourite_group")
@Table(schema = "public", name = "favourite_group")
public class FavouriteGroup extends Favourite {

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@Override
	public EnumFavouriteType getType() {
		return EnumFavouriteType.GROUP;
	}

}
