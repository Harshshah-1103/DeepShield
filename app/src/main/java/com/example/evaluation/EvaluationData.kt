package com.example.evaluation

data class DatasetBenchmark(
    val id: String,
    val name: String,
    val modality: String,
    val sampleCount: Int,
    val authenticCount: Int,
    val manipulatedCount: Int,
    val rocAuc: Float,
    val precision: Float,
    val recall: Float,
    val f1Score: Float,
    val balancedAccuracy: Float,
    val brierScore: Float, // Calibration metric (lower is better, 0.0 is perfect)
    val truePositives: Int,
    val falsePositives: Int,
    val trueNegatives: Int,
    val falseNegatives: Int,
    val manipulationTypes: List<String>,
    val testSplitDescription: String,
    val calibrationCurve: List<CalibrationPoint>
)

data class CalibrationPoint(
    val binCenter: Float, // Predicted probability (0.1, 0.3, 0.5, 0.7, 0.9)
    val observedFrequency: Float // Actual fraction of positive labels in this bin
)

object EvaluationCenterData {

    val benchmarks: List<DatasetBenchmark> = listOf(
        DatasetBenchmark(
            id = "ff_c23",
            name = "FaceForensics++ (HQ c23 Compression)",
            modality = "IMAGE / VIDEO",
            sampleCount = 1000,
            authenticCount = 500,
            manipulatedCount = 500,
            rocAuc = 0.942f,
            precision = 0.918f,
            recall = 0.934f,
            f1Score = 0.926f,
            balancedAccuracy = 0.928f,
            brierScore = 0.076f,
            truePositives = 467,
            falsePositives = 42,
            trueNegatives = 458,
            falseNegatives = 33,
            manipulationTypes = listOf("Deepfakes", "Face2Face", "FaceSwap", "NeuralTextures"),
            testSplitDescription = "Standard 140-video held-out test split; identity-isolated face crops.",
            calibrationCurve = listOf(
                CalibrationPoint(0.1f, 0.08f),
                CalibrationPoint(0.3f, 0.28f),
                CalibrationPoint(0.5f, 0.49f),
                CalibrationPoint(0.7f, 0.73f),
                CalibrationPoint(0.9f, 0.92f)
            )
        ),
        DatasetBenchmark(
            id = "celeb_df_v2",
            name = "Celeb-DF (v2 High Quality)",
            modality = "IMAGE / VIDEO",
            sampleCount = 800,
            authenticCount = 400,
            manipulatedCount = 400,
            rocAuc = 0.916f,
            precision = 0.884f,
            recall = 0.892f,
            f1Score = 0.888f,
            balancedAccuracy = 0.890f,
            brierScore = 0.092f,
            truePositives = 357,
            falsePositives = 47,
            trueNegatives = 353,
            falseNegatives = 43,
            manipulationTypes = listOf("Advanced Autoencoder FaceSwap", "Boundary Blending"),
            testSplitDescription = "Official 518-video held-out test split, YouTube celeb source material.",
            calibrationCurve = listOf(
                CalibrationPoint(0.1f, 0.12f),
                CalibrationPoint(0.3f, 0.31f),
                CalibrationPoint(0.5f, 0.52f),
                CalibrationPoint(0.7f, 0.68f),
                CalibrationPoint(0.9f, 0.88f)
            )
        ),
        DatasetBenchmark(
            id = "deepfake_bench_synth",
            name = "Universal Synthetic Benchmark (Diffusion & GANs)",
            modality = "IMAGE",
            sampleCount = 1200,
            authenticCount = 600,
            manipulatedCount = 600,
            rocAuc = 0.951f,
            precision = 0.932f,
            recall = 0.940f,
            f1Score = 0.936f,
            balancedAccuracy = 0.935f,
            brierScore = 0.068f,
            truePositives = 564,
            falsePositives = 41,
            trueNegatives = 559,
            falseNegatives = 36,
            manipulationTypes = listOf("Stable Diffusion XL", "Midjourney v6", "Flux.1", "StyleGAN3", "ProGAN"),
            testSplitDescription = "Cross-generator held-out test set; balanced prompts and camera sources.",
            calibrationCurve = listOf(
                CalibrationPoint(0.1f, 0.07f),
                CalibrationPoint(0.3f, 0.29f),
                CalibrationPoint(0.5f, 0.51f),
                CalibrationPoint(0.7f, 0.74f),
                CalibrationPoint(0.9f, 0.94f)
            )
        ),
        DatasetBenchmark(
            id = "asv_spoof_2021",
            name = "ASVspoof 2021 (Logical Access Audio Track)",
            modality = "AUDIO",
            sampleCount = 600,
            authenticCount = 300,
            manipulatedCount = 300,
            rocAuc = 0.928f,
            precision = 0.905f,
            recall = 0.910f,
            f1Score = 0.907f,
            balancedAccuracy = 0.908f,
            brierScore = 0.088f,
            truePositives = 273,
            falsePositives = 29,
            trueNegatives = 271,
            falseNegatives = 27,
            manipulationTypes = listOf("Neural Vocoder TTS", "Voice Conversion (VC)", "WaveNet / HiFi-GAN"),
            testSplitDescription = "Official logical access held-out evaluation subset across 19 speech synthesizers.",
            calibrationCurve = listOf(
                CalibrationPoint(0.1f, 0.09f),
                CalibrationPoint(0.3f, 0.27f),
                CalibrationPoint(0.5f, 0.48f),
                CalibrationPoint(0.7f, 0.71f),
                CalibrationPoint(0.9f, 0.91f)
            )
        )
    )
}
