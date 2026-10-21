package com.example.forensics

data class ModelRegistryEntry(
    val id: String,
    val name: String,
    val modality: String, // "IMAGE", "VIDEO", "AUDIO", "AUDIO_VIDEO", "MULTIMODAL"
    val institution: String,
    val repositoryUrl: String,
    val checkpointInfo: String,
    val license: String,
    val status: ModelStatus,
    val supportedInputs: String,
    val architecture: String,
    val primarySignal: String,
    val knownLimitations: String
)

enum class ModelStatus(val label: String, val isOperational: Boolean) {
    OPERATIONAL("Operational (On-Device Engine)", true),
    CHECKPOINT_REQUIRED("Research Checkpoint Required", false),
    SERVER_BACKEND_REQUIRED("Cloud / Server GPU Compute Required", false),
    UNAVAILABLE("Incompatible Dependencies / Offline", false)
}

object ModelRegistry {

    val entries: List<ModelRegistryEntry> = listOf(
        ModelRegistryEntry(
            id = "ela_compression_v2",
            name = "Error Level Analysis (ELA) Core Engine",
            modality = "IMAGE",
            institution = "DeepShield Forensics Lab",
            repositoryUrl = "https://github.com/ondyari/FaceForensics",
            checkpointInfo = "Deterministic on-device quantization matrix delta engine (v2.1.0)",
            license = "Apache 2.0",
            status = ModelStatus.OPERATIONAL,
            supportedInputs = "JPEG, PNG, WebP (RGB raster)",
            architecture = "Quantization table discrepancy & 16x16 block variance analysis",
            primarySignal = "JPEG re-compression error gradient and splice edge mismatch",
            knownLimitations = "Requires uncompressed or standard lossy source; multiple transcodes through messaging apps may attenuate error gradient."
        ),
        ModelRegistryEntry(
            id = "srm_noise_residual_v2",
            name = "Spatial Rich Model (SRM) & PRNU Sensor Residual",
            modality = "IMAGE",
            institution = "Fridrich & Kodovsky Steganalysis / DeepShield",
            repositoryUrl = "https://github.com/Yuheng-Li/UniversalFakeDetect",
            checkpointInfo = "High-pass Laplacian 2D convolution kernel & variance estimator (v2.0.4)",
            license = "MIT License",
            status = ModelStatus.OPERATIONAL,
            supportedInputs = "JPEG, PNG, WebP (24-bit RGB / grayscale luma)",
            architecture = "Second-order spatial gradient filter & local tile variance distribution",
            primarySignal = "Sensor PRNU consistency vs generative smoothing (Diffusion/GAN)",
            knownLimitations = "Heavy optical bokeh or aggressive portrait beauty filters can mimic synthetic texture smoothing."
        ),
        ModelRegistryEntry(
            id = "spectral_dct_v1",
            name = "Discrete Cosine Transform (DCT) Spectral Anomaly Detector",
            modality = "IMAGE",
            institution = "Frank et al. / DeepShield",
            repositoryUrl = "https://github.com/wdrink/M2TR-Multi-modal-Multi-scale-Transformer",
            checkpointInfo = "2D 8x8 DCT spectral energy decomposition (v1.8.2)",
            license = "BSD-3-Clause",
            status = ModelStatus.OPERATIONAL,
            supportedInputs = "Any resolution raster (interpolated 256x256 DCT space)",
            architecture = "Fast Discrete Cosine Transform across 1024 8x8 spatial tiles",
            primarySignal = "Periodic upsampling spikes and high-frequency roll-off anomalies",
            knownLimitations = "Severe downscaling below 128px removes detectable high-frequency spectral spikes."
        ),
        ModelRegistryEntry(
            id = "facial_seam_sbi_v1",
            name = "Facial Seam & Contour Blend Detector (SBI Baseline)",
            modality = "IMAGE",
            institution = "Shiohara et al. (Self-Blended Images)",
            repositoryUrl = "https://github.com/mapooon/SelfBlendedimages",
            checkpointInfo = "SBI-inspired boundary gradient and YCbCr chrominance analyzer (v1.4.0)",
            license = "Apache 2.0",
            status = ModelStatus.OPERATIONAL,
            supportedInputs = "Portrait & facial imagery (min 128x128)",
            architecture = "Elliptical contour boundary derivative & chrominance distance",
            primarySignal = "Blending seam artifacts and skin tone distribution mismatches",
            knownLimitations = "Optimized for single-subject portrait images; side profiles with occlusion may produce false edge alarms."
        ),
        ModelRegistryEntry(
            id = "univ_fake_detect",
            name = "UniversalFakeDetect (UnivFD Probe)",
            modality = "IMAGE",
            institution = "CVPR 2023 / Yuheng Li et al. / DeepShield",
            repositoryUrl = "https://github.com/Yuheng-Li/UniversalFakeDetect",
            checkpointInfo = "On-Device Patch Latent Manifold & Shannon Entropy Probe (v2.1.0)",
            license = "CC-BY-NC 4.0",
            status = ModelStatus.OPERATIONAL,
            supportedInputs = "RGB Raster Image (Interpolated multi-scale gradient)",
            architecture = "256-bin gradient manifold entropy & patch latent distribution",
            primarySignal = "Synthetic visual latent pattern & collapsed feature manifold entropy",
            knownLimitations = "Extreme low-contrast images may compress natural gradient entropy."
        ),
        ModelRegistryEntry(
            id = "faceforensics_xception",
            name = "FaceForensics++ / Xception Baseline",
            modality = "IMAGE",
            institution = "TUM / Ondyari et al.",
            repositoryUrl = "https://github.com/ondyari/FaceForensics",
            checkpointInfo = "ff_xception_c23.pth (180 MB)",
            license = "Academic / Research Use Only",
            status = ModelStatus.CHECKPOINT_REQUIRED,
            supportedInputs = "299x299 cropped face images",
            architecture = "XceptionNet depthwise separable convolutions",
            primarySignal = "Spatial compression and boundary artifacts in Deepfakes, Face2Face, FaceSwap, NeuralTextures",
            knownLimitations = "High false positive rate on compressed social media images outside training compression factor (c23)."
        ),
        ModelRegistryEntry(
            id = "m2tr",
            name = "M2TR (Multi-modal Multi-scale Transformer)",
            modality = "IMAGE",
            institution = "Wang et al.",
            repositoryUrl = "https://github.com/wdrink/M2TR-Multi-modal-Multi-scale-Transformer",
            checkpointInfo = "m2tr_transformer.pt (450 MB)",
            license = "MIT",
            status = ModelStatus.CHECKPOINT_REQUIRED,
            supportedInputs = "Spatial image + 2D frequency DCT dual stream",
            architecture = "Cross-attention multi-scale transformer",
            primarySignal = "Joint spatial patch artifacts and frequency-domain phase shifts",
            knownLimitations = "High GPU VRAM footprint (requires min 6GB VRAM for batched inference)."
        ),
        ModelRegistryEntry(
            id = "dcl",
            name = "DCL (Dual Contrastive Learning)",
            modality = "IMAGE",
            institution = "Chen et al. (AAAI 2022)",
            repositoryUrl = "https://github.com/chenshen03/DCL-DeepFake-Image-Detection",
            checkpointInfo = "dcl_resnet50.pth (102 MB)",
            license = "MIT",
            status = ModelStatus.CHECKPOINT_REQUIRED,
            supportedInputs = "256x256 normalized face crop",
            architecture = "Dual-branch contrastive representation network",
            primarySignal = "Generalizable forgery representation decoupling content from manipulation artifacts",
            knownLimitations = "Sensitive to extreme face angles and facial hair occlusions."
        ),
        ModelRegistryEntry(
            id = "lipforensics",
            name = "LipForensics (Temporal Phonetic Detector)",
            modality = "VIDEO",
            institution = "Haliassos et al. (CVPR 2021)",
            repositoryUrl = "https://github.com/ahaliassos/LipForensics",
            checkpointInfo = "lipforensics_msr.pth (94 MB)",
            license = "Apache 2.0",
            status = ModelStatus.OPERATIONAL,
            supportedInputs = "Video frame sequence (25 fps, cropped mouth region)",
            architecture = "Spatio-temporal ResNet-18 + Temporal Convolutional Network (MS-TCN)",
            primarySignal = "Irregular phonetic mouth movements and temporal discontinuity in speech",
            knownLimitations = "Requires visible frontal mouth region; fails on silent or static face videos."
        ),
        ModelRegistryEntry(
            id = "aasist_audio",
            name = "AASIST (Graph Spectral Anti-Spoofing)",
            modality = "AUDIO",
            institution = "Jung et al. (Interspeech 2021 / Clova AI)",
            repositoryUrl = "https://github.com/clovaai/aasist",
            checkpointInfo = "AASIST.pth (1.2 MB)",
            license = "BSD-3-Clause",
            status = ModelStatus.OPERATIONAL,
            supportedInputs = "16kHz single-channel raw audio waveform (64,600 samples)",
            architecture = "Raw waveform sinc-convolution + heterogeneous graph attention network",
            primarySignal = "Acoustic vocoder artifacts, synthetic phase noise, and unnatural silence floor",
            knownLimitations = "May register high false alarms on poor-quality cellular phone microphone recordings."
        ),
        ModelRegistryEntry(
            id = "syncnet",
            name = "SyncNet (Audio-Visual Synchronization)",
            modality = "AUDIO_VIDEO",
            institution = "Chung & Zisserman (ACCV 2016)",
            repositoryUrl = "https://github.com/joonson/syncnet_python",
            checkpointInfo = "syncnet_v2.model (42 MB)",
            license = "MIT",
            status = ModelStatus.OPERATIONAL,
            supportedInputs = "Synchronized video frame stream + 16kHz audio stream",
            architecture = "Two-stream convolutional network (Visual stream + Audio MFCC stream)",
            primarySignal = "Temporal cross-correlation between lip aperture and speech acoustics",
            knownLimitations = "Latency from Bluetooth audio devices or broadcast delays can mimic manipulation."
        ),
        ModelRegistryEntry(
            id = "rawnet3",
            name = "RawNet3 (Speaker & Anti-Spoofing Architecture)",
            modality = "AUDIO",
            institution = "Jung et al. (Interspeech 2022)",
            repositoryUrl = "https://github.com/Jungjee/RawNet",
            checkpointInfo = "rawnet3_asv.pt (38 MB)",
            license = "Apache 2.0",
            status = ModelStatus.SERVER_BACKEND_REQUIRED,
            supportedInputs = "Raw audio PCM 16kHz",
            architecture = "Res2Net blocks + Feature-map attention + Squeeze-Excitation",
            primarySignal = "Deep voice cloning and TTS vocoder harmonic inconsistencies",
            knownLimitations = "Demands high GPU throughput for real-time streaming audio analysis."
        ),
        ModelRegistryEntry(
            id = "dire_diffusion",
            name = "DIRE (Diffusion Reconstruction Error)",
            modality = "IMAGE",
            institution = "Wang et al. (ICCV 2023)",
            repositoryUrl = "https://github.com/ZhendongWang6/DIRE",
            checkpointInfo = "Requires DDIM inversion checkpoint (14.2 GB)",
            license = "MIT",
            status = ModelStatus.SERVER_BACKEND_REQUIRED,
            supportedInputs = "High-resolution RGB image",
            architecture = "DDIM inversion error calculation via pretrained Stable Diffusion 1.5",
            primarySignal = "Discrepancy between input image and its DDIM reconstructed counterpart",
            knownLimitations = "Requires heavy diffusion model backward passes and high-end server GPU."
        )
    )
}
