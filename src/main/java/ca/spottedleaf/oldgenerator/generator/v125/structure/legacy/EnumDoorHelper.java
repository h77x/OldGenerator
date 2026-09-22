package ca.spottedleaf.oldgenerator.generator.v125.structure.legacy;
public final class EnumDoorHelper {
    public static final int[] doorEnum = new int[EnumDoor.values().length];
    static {
        doorEnum[EnumDoor.OPENING.ordinal()] = 1;
        doorEnum[EnumDoor.WOOD_DOOR.ordinal()] = 2;
        doorEnum[EnumDoor.GRATES.ordinal()] = 3;
        doorEnum[EnumDoor.IRON_DOOR.ordinal()] = 4;
    }
}
