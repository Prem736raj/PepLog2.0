package com.appvexis.peptidetracker.core.model

/**
 * Lifecycle status of a peptide cycle stack.
 */
enum class ProtocolStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    ARCHIVED
}

/**
 * Injection/administration frequency modes.
 */
enum class FrequencyType {
    DAILY,
    WEEKLY,
    CUSTOM,  // E.g., Specific days of week [Monday, Thursday]
    CYCLE    // E.g., "5 days on, 2 days off"
}

/**
 * Dosing metrics supported.
 */
enum class DoseUnit {
    MG,
    MCG,
    IU
}

/**
 * Mode of peptide delivery.
 */
enum class AdminRoute {
    SUBQ,
    IM,
    INTRANASAL,
    ORAL,
    TOPICAL
}

/**
 * Logging status of a scheduled dose.
 */
enum class DoseStatus {
    PENDING,
    TAKEN,
    MISSED,
    SKIPPED
}

/**
 * State of local tissue recovery at an injection site.
 */
enum class HealingStatus {
    OK,
    BRUISED,
    LUMP,
    REDNESS
}

/**
 * Lifecycle status of a peptide vial in user stock.
 */
enum class InventoryStatus {
    IN_STOCK,
    IN_USE,
    EXPIRED,
    EMPTY
}

/**
 * Anatomical injection site zones for the body map.
 * SubQ = Subcutaneous, IM = Intramuscular.
 */
enum class InjectionSiteArea(val displayName: String, val category: String) {
    // Subcutaneous - Abdomen (4 quadrants)
    ABDOMEN_UPPER_LEFT("Abdomen Upper Left", "SubQ"),
    ABDOMEN_UPPER_RIGHT("Abdomen Upper Right", "SubQ"),
    ABDOMEN_LOWER_LEFT("Abdomen Lower Left", "SubQ"),
    ABDOMEN_LOWER_RIGHT("Abdomen Lower Right", "SubQ"),

    // Subcutaneous - Thighs
    THIGH_OUTER_LEFT("Left Outer Thigh", "SubQ"),
    THIGH_OUTER_RIGHT("Right Outer Thigh", "SubQ"),
    THIGH_INNER_LEFT("Left Inner Thigh", "SubQ"),
    THIGH_INNER_RIGHT("Right Inner Thigh", "SubQ"),

    // Subcutaneous - Upper Arms
    UPPER_ARM_LEFT("Left Upper Arm", "SubQ"),
    UPPER_ARM_RIGHT("Right Upper Arm", "SubQ"),

    // Subcutaneous - Upper Buttocks
    UPPER_BUTTOCK_LEFT("Left Upper Buttock", "SubQ"),
    UPPER_BUTTOCK_RIGHT("Right Upper Buttock", "SubQ"),

    // Intramuscular
    DELTOID_LEFT("Left Deltoid", "IM"),
    DELTOID_RIGHT("Right Deltoid", "IM"),
    VASTUS_LATERALIS_LEFT("Left Vastus Lateralis", "IM"),
    VASTUS_LATERALIS_RIGHT("Right Vastus Lateralis", "IM"),
    VENTROGLUTEAL_LEFT("Left Ventrogluteal", "IM"),
    VENTROGLUTEAL_RIGHT("Right Ventrogluteal", "IM");

    companion object {
        fun fromString(value: String): InjectionSiteArea? =
            entries.find { it.name == value }
    }
}
