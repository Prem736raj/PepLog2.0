package com.appvexis.peptidetracker.feature.progress.model

import com.appvexis.peptidetracker.core.model.BiomarkerLog
import com.appvexis.peptidetracker.core.model.ProgressPhoto
import com.appvexis.peptidetracker.core.model.Protocol
import com.appvexis.peptidetracker.core.model.SideEffectLog

enum class ProgressTab(val displayName: String) {
    PHOTOS("Photos"),
    BIOMARKERS("Biomarkers"),
    SUBJECTIVE("Wellness & Mood"),
    BODY_METRICS("Body Metrics")
}

enum class PhotoCategory(val displayName: String) {
    ALL("All Photos"),
    FRONT("Front View"),
    SIDE("Side View"),
    BACK("Back View"),
    DETAIL("Detail / Close-up"),
    OTHER("Other")
}

enum class BiomarkerCategory(val displayName: String) {
    HORMONES("Hormones & Growth"),
    METABOLIC("Metabolic & Glycemic"),
    LIPIDS("Cardiovascular & Lipids"),
    BLOOD_CBC("Complete Blood Count (CBC)"),
    KIDNEY_ELECTROLYTES("Kidney & Electrolytes"),
    LIVER_ORGAN("Liver & Metabolic Organ"),
    OTHER("Other Biomarkers")
}

/**
 * Standard definition metadata for 50+ clinical biomarkers.
 */
data class BiomarkerDefinition(
    val name: String,
    val category: BiomarkerCategory,
    val defaultUnit: String,
    val rangeLow: Double?,
    val rangeHigh: Double?,
    val description: String
)

/**
 * Predefined catalog of 50+ biomarkers relevant to peptide therapy, longevity, and metabolic optimization.
 */
object BiomarkerCatalog {
    val definitions = listOf(
        // 1. Hormones & Growth Factors
        BiomarkerDefinition("IGF-1 (Somatomedin C)", BiomarkerCategory.HORMONES, "ng/mL", 115.0, 307.0, "Primary biomarker for growth hormone secretion & secretagogue efficacy."),
        BiomarkerDefinition("Total Testosterone", BiomarkerCategory.HORMONES, "ng/dL", 300.0, 1000.0, "Total circulating androgenic hormone levels."),
        BiomarkerDefinition("Free Testosterone", BiomarkerCategory.HORMONES, "pg/mL", 35.0, 155.0, "Bioavailable unbound testosterone active in tissues."),
        BiomarkerDefinition("Estradiol (E2 - Sensitive)", BiomarkerCategory.HORMONES, "pg/mL", 10.0, 40.0, "Primary estrogen hormone; balance with androgens."),
        BiomarkerDefinition("DHEA-Sulfate", BiomarkerCategory.HORMONES, "µg/dL", 160.0, 449.0, "Adrenal steroid precursor reflecting vitality & recovery."),
        BiomarkerDefinition("Growth Hormone (GH)", BiomarkerCategory.HORMONES, "ng/mL", 0.05, 5.0, "Pulsatile pituitary somatotropin level."),
        BiomarkerDefinition("Prolactin", BiomarkerCategory.HORMONES, "ng/mL", 2.0, 18.0, "Pituitary hormone; monitor for dopaminergic balance."),
        BiomarkerDefinition("Cortisol (AM Fasting)", BiomarkerCategory.HORMONES, "µg/dL", 6.0, 23.0, "Primary glucocorticoid stress & circadian marker."),
        BiomarkerDefinition("SHBG", BiomarkerCategory.HORMONES, "nmol/L", 16.5, 55.9, "Sex hormone-binding globulin regulator."),
        BiomarkerDefinition("Luteinizing Hormone (LH)", BiomarkerCategory.HORMONES, "mIU/mL", 1.7, 8.6, "Pituitary gonadotropin stimulating steroidogenesis."),
        BiomarkerDefinition("FSH", BiomarkerCategory.HORMONES, "mIU/mL", 1.5, 12.4, "Follicle-stimulating hormone regulating fertility."),
        BiomarkerDefinition("IGFBP-3", BiomarkerCategory.HORMONES, "mg/L", 3.4, 7.0, "Major binding protein for circulating IGF-1."),
        BiomarkerDefinition("TSH (Thyroid Stimulating)", BiomarkerCategory.HORMONES, "µIU/mL", 0.45, 4.5, "Thyroid axis regulation marker."),
        BiomarkerDefinition("Free T3", BiomarkerCategory.HORMONES, "pg/mL", 2.0, 4.4, "Active cellular thyroid hormone."),
        BiomarkerDefinition("Free T4", BiomarkerCategory.HORMONES, "ng/dL", 0.82, 1.77, "Circulating prohormone for T3 conversion."),
        BiomarkerDefinition("Reverse T3", BiomarkerCategory.HORMONES, "ng/dL", 9.2, 24.1, "Inactive thyroid metabolite during stress/inflammation."),

        // 2. Metabolic & Glycemic
        BiomarkerDefinition("Fasting Blood Glucose", BiomarkerCategory.METABOLIC, "mg/dL", 70.0, 99.0, "Baseline glycemic control marker."),
        BiomarkerDefinition("HbA1c (Glycated Hemoglobin)", BiomarkerCategory.METABOLIC, "%", 4.0, 5.6, "3-month average blood glucose control."),
        BiomarkerDefinition("Fasting Insulin", BiomarkerCategory.METABOLIC, "µIU/mL", 2.0, 6.0, "Key metric for insulin sensitivity and metabolic health."),
        BiomarkerDefinition("HOMA-IR Score", BiomarkerCategory.METABOLIC, "index", 0.5, 1.5, "Homeostatic model assessment of insulin resistance."),
        BiomarkerDefinition("C-Peptide", BiomarkerCategory.METABOLIC, "ng/mL", 0.8, 3.1, "Direct measure of endogenous pancreatic beta-cell insulin synthesis."),
        BiomarkerDefinition("Leptin", BiomarkerCategory.METABOLIC, "ng/mL", 1.2, 9.5, "Satiety hormone regulating energy expenditure."),
        BiomarkerDefinition("Adiponectin", BiomarkerCategory.METABOLIC, "µg/mL", 4.0, 20.0, "Adipokine enhancing insulin sensitivity and fatty acid oxidation."),

        // 3. Cardiovascular & Lipids
        BiomarkerDefinition("Total Cholesterol", BiomarkerCategory.LIPIDS, "mg/dL", 125.0, 200.0, "Total serum sterol concentration."),
        BiomarkerDefinition("HDL Cholesterol", BiomarkerCategory.LIPIDS, "mg/dL", 40.0, 80.0, "High-density lipoprotein ('good' cholesterol)."),
        BiomarkerDefinition("LDL Cholesterol (Direct)", BiomarkerCategory.LIPIDS, "mg/dL", 50.0, 100.0, "Low-density atherogenic lipoprotein particles."),
        BiomarkerDefinition("Triglycerides", BiomarkerCategory.LIPIDS, "mg/dL", 40.0, 150.0, "Circulating neutral fats from metabolism."),
        BiomarkerDefinition("Apolipoprotein B (ApoB)", BiomarkerCategory.LIPIDS, "mg/dL", 50.0, 90.0, "Total count of atherogenic particles."),
        BiomarkerDefinition("Apolipoprotein A1 (ApoA1)", BiomarkerCategory.LIPIDS, "mg/dL", 119.0, 240.0, "Primary structural protein in HDL particles."),
        BiomarkerDefinition("Lipoprotein(a) / Lp(a)", BiomarkerCategory.LIPIDS, "nmol/L", 0.0, 75.0, "Genetic cardiovascular risk particle."),
        BiomarkerDefinition("High-Sensitivity CRP (hs-CRP)", BiomarkerCategory.LIPIDS, "mg/L", 0.0, 1.0, "Systemic vascular and tissue inflammation marker."),
        BiomarkerDefinition("Homocysteine", BiomarkerCategory.LIPIDS, "µmol/L", 4.0, 10.0, "Methylation and endothelial vascular health indicator."),

        // 4. CBC & Blood Metrics
        BiomarkerDefinition("Hemoglobin", BiomarkerCategory.BLOOD_CBC, "g/dL", 13.5, 17.5, "Oxygen-carrying protein in red blood cells."),
        BiomarkerDefinition("Hematocrit", BiomarkerCategory.BLOOD_CBC, "%", 38.8, 50.0, "Volume percentage of red blood cells in blood."),
        BiomarkerDefinition("Red Blood Cell Count (RBC)", BiomarkerCategory.BLOOD_CBC, "M/µL", 4.3, 5.9, "Total erythrocyte concentration."),
        BiomarkerDefinition("White Blood Cell Count (WBC)", BiomarkerCategory.BLOOD_CBC, "K/µL", 4.0, 11.0, "Total leukocyte immune cell concentration."),
        BiomarkerDefinition("Platelet Count", BiomarkerCategory.BLOOD_CBC, "K/µL", 150.0, 450.0, "Thrombocytes involved in clot formation & healing."),
        BiomarkerDefinition("Ferritin", BiomarkerCategory.BLOOD_CBC, "ng/mL", 30.0, 400.0, "Intracellular iron storage protein."),
        BiomarkerDefinition("Serum Iron", BiomarkerCategory.BLOOD_CBC, "µg/dL", 60.0, 170.0, "Circulating iron bound to transferrin."),

        // 5. Kidney & Electrolytes
        BiomarkerDefinition("eGFR (Glomerular Filtration)", BiomarkerCategory.KIDNEY_ELECTROLYTES, "mL/min/1.73m²", 90.0, 130.0, "Estimated kidney filtration rate."),
        BiomarkerDefinition("Serum Creatinine", BiomarkerCategory.KIDNEY_ELECTROLYTES, "mg/dL", 0.7, 1.3, "Muscle breakdown waste excreted by kidneys."),
        BiomarkerDefinition("Blood Urea Nitrogen (BUN)", BiomarkerCategory.KIDNEY_ELECTROLYTES, "mg/dL", 6.0, 24.0, "Nitrogenous protein metabolic waste."),
        BiomarkerDefinition("Cystatin C", BiomarkerCategory.KIDNEY_ELECTROLYTES, "mg/L", 0.5, 1.0, "Muscle-mass independent renal function marker."),
        BiomarkerDefinition("Sodium", BiomarkerCategory.KIDNEY_ELECTROLYTES, "mEq/L", 135.0, 145.0, "Extracellular electrolyte and fluid balance."),
        BiomarkerDefinition("Potassium", BiomarkerCategory.KIDNEY_ELECTROLYTES, "mEq/L", 3.5, 5.2, "Intracellular electrolyte for cardiac & neuromuscular function."),
        BiomarkerDefinition("Magnesium (Serum)", BiomarkerCategory.KIDNEY_ELECTROLYTES, "mg/dL", 1.8, 2.4, "Essential cofactor in 300+ enzymatic reactions."),

        // 6. Liver & Organ Health
        BiomarkerDefinition("ALT (Alanine Aminotransferase)", BiomarkerCategory.LIVER_ORGAN, "U/L", 9.0, 40.0, "Liver-specific hepatocellular enzyme."),
        BiomarkerDefinition("AST (Aspartate Aminotransferase)", BiomarkerCategory.LIVER_ORGAN, "U/L", 10.0, 40.0, "Enzyme found in liver, cardiac and skeletal muscle."),
        BiomarkerDefinition("GGT (Gamma-Glutamyl Transferase)", BiomarkerCategory.LIVER_ORGAN, "U/L", 9.0, 48.0, "Biliary and oxidative liver stress marker."),
        BiomarkerDefinition("Total Bilirubin", BiomarkerCategory.LIVER_ORGAN, "mg/dL", 0.2, 1.2, "Heme breakdown product cleared by liver."),
        BiomarkerDefinition("Alkaline Phosphatase (ALP)", BiomarkerCategory.LIVER_ORGAN, "U/L", 44.0, 147.0, "Biliary duct and bone turnover enzyme."),
        BiomarkerDefinition("Albumin", BiomarkerCategory.LIVER_ORGAN, "g/dL", 3.5, 5.5, "Major circulating protein synthesized by liver.")
    )

    fun findDefinition(name: String): BiomarkerDefinition? {
        return definitions.firstOrNull { it.name.equals(name, ignoreCase = true) || it.name.startsWith(name, ignoreCase = true) }
    }
}

/**
 * UI model representing a logged biomarker with its computed status (Low, In Range, High).
 */
data class BiomarkerUiModel(
    val log: BiomarkerLog,
    val category: BiomarkerCategory,
    val rangeLow: Double?,
    val rangeHigh: Double?,
    val formattedDate: String,
    val statusText: String,
    val isOptimal: Boolean
)

/**
 * UI representation of a day's subjective check-in.
 */
data class DailySubjectiveCheckIn(
    val date: Long,
    val formattedDate: String,
    val mood: Int?,
    val energy: Int?,
    val sleep: Int?,
    val pain: Int?,
    val libido: Int?,
    val sideEffects: List<SideEffectLog>,
    val notes: String?
)

/**
 * Body metrics record (weight, fat %, muscle, waist).
 */
data class BodyMetricUiModel(
    val date: Long,
    val formattedDate: String,
    val weightKg: Double?,
    val bodyFatPercent: Double?,
    val muscleMassKg: Double?,
    val waistCm: Double?,
    val deltaWeightKg: Double?,
    val notes: String?
)

/**
 * Comprehensive UI State for the Progress Screen.
 */
sealed interface ProgressUiState {
    data object Loading : ProgressUiState
    data class Success(
        val activeTab: ProgressTab = ProgressTab.PHOTOS,
        val photos: List<ProgressPhoto> = emptyList(),
        val selectedCategory: PhotoCategory = PhotoCategory.ALL,
        val biomarkerLogs: List<BiomarkerUiModel> = emptyList(),
        val subjectiveCheckIns: List<DailySubjectiveCheckIn> = emptyList(),
        val latestCheckIn: DailySubjectiveCheckIn? = null,
        val bodyMetrics: List<BodyMetricUiModel> = emptyList(),
        val latestWeight: Double? = null,
        val weightDeltaTotal: Double? = null,
        val activeProtocols: List<Protocol> = emptyList()
    ) : ProgressUiState
}
