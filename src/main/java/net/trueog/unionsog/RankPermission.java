package net.trueog.unionsog;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * 
 * @author RoinujNosde
 *
 */
public enum RankPermission {

    ALLY_ADD("unionsog.leader.ally", PermissionLevel.LEADER),
    ALLY_CHAT("unionsog.member.ally", PermissionLevel.TRUSTED),
    ALLY_REMOVE("unionsog.leader.ally", PermissionLevel.LEADER),
    BANK_BALANCE("unionsog.member.bank", PermissionLevel.TRUSTED),
    BANK_DEPOSIT("unionsog.member.bank", PermissionLevel.LEADER),
    BANK_WITHDRAW("unionsog.member.bank", PermissionLevel.LEADER),
    BB_ADD("unionsog.member.bb-add", PermissionLevel.TRUSTED),
    BB_CLEAR("unionsog.leader.bb-clear", PermissionLevel.LEADER),
    COORDS("unionsog.member.coords", PermissionLevel.TRUSTED),
    REGROUP_HOME("unionsog.leader.regroup.home", PermissionLevel.LEADER),
    REGROUP_ME("unionsog.leader.regroup.me", PermissionLevel.LEADER),
    HOME_SET("unionsog.leader.home-set", PermissionLevel.LEADER),
    HOME_TP("unionsog.member.home", PermissionLevel.TRUSTED), INVITE("unionsog.leader.invite", PermissionLevel.LEADER),
    KICK("unionsog.leader.kick", PermissionLevel.LEADER), COLOR("unionsog.leader.color", PermissionLevel.LEADER),
    RANK_DISPLAYNAME("unionsog.leader.rank.setdisplayname", PermissionLevel.LEADER),
    RANK_LIST("unionsog.leader.rank.list", PermissionLevel.LEADER),
    RIVAL_ADD("unionsog.leader.rival", PermissionLevel.LEADER),
    RIVAL_REMOVE("unionsog.leader.rival", PermissionLevel.LEADER),
    WAR_END("unionsog.leader.war", PermissionLevel.LEADER), WAR_START("unionsog.leader.war", PermissionLevel.LEADER),
    STATS("unionsog.member.stats", PermissionLevel.TRUSTED), VITALS("unionsog.member.vitals", PermissionLevel.TRUSTED),
    KILLS("unionsog.member.kills", PermissionLevel.TRUSTED),
    DESCRIPTION("unionsog.leader.description", PermissionLevel.LEADER),
    MOSTKILLED("unionsog.mod.mostkilled", PermissionLevel.TRUSTED),
    FRIENDLYFIRE("unionsog.leader.ff", PermissionLevel.LEADER),
    SETBANNER("unionsog.leader.setbanner", PermissionLevel.LEADER);

    private final String bukkitPermission;
    private final PermissionLevel permissionLevel;

    RankPermission(String bukkitPermission, PermissionLevel permissionLevel) {

        this.bukkitPermission = bukkitPermission;
        this.permissionLevel = permissionLevel;

    }

    /**
     *
     * @return the Bukkit equivalent to this rank permission
     */
    public String getBukkitPermission() {

        return bukkitPermission;

    }

    /**
     * 
     * @return the PermissionLevel
     *
     */
    public PermissionLevel getPermissionLevel() {

        return permissionLevel;

    }

    @Override
    public String toString() {

        return super.toString().replace("_", ".").toLowerCase();

    }

    /**
     * 
     * @param permission the permission
     * @return true if this is a valid rank permission
     */
    @Contract("null -> false")
    public static boolean isValid(@Nullable String permission) {

        for (RankPermission p : values()) {

            if (p.toString().equalsIgnoreCase(permission)) {

                return true;

            }

        }

        return false;

    }

}
