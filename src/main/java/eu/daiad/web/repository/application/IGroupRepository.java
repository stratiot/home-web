package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.group.Account;
import eu.daiad.web.model.group.Group;
import eu.daiad.web.model.group.GroupQuery;
import eu.daiad.web.model.query.EnumClusterType;

public interface IGroupRepository {

    List<Group> getAll(GroupQuery query);

    List<Group> getUtilities(UUID utilityKey);

    List<Group> getClusters(UUID utilityKey);

    List<Group> getClusterByKeySegments(UUID clusterKey);

    List<Group> getClusterByNameSegments(String name);

    List<Group> getClusterByTypeSegments(EnumClusterType type);

    List<Group> getSets();

    List<Group> getCommunities();

    List<Account> getGroupMembers(UUID groupKey);

    List<UUID> getGroupMemberKeys(UUID groupKey);

    List<UUID> getUtilityByIdMemberKeys(int utilityId);

    List<UUID> getUtilityByKeyMemberKeys(UUID utilityKey);

}
