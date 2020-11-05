package io.flutter.plugins.camera;

import android.hardware.camera2.CameraCharacteristics;

public class CameraCompatibility {
    private final CameraCharacteristics characteristics;

    CameraCompatibility(CameraCharacteristics characteristics) {
        this.characteristics = characteristics;
    }

    private int supportedHardwareLevel() {
        return characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    }

    public boolean isFlashSupported() {
        return characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) != null;
    }

    // Sensor sensitivity (ISO)
    // https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#SENSOR_SENSITIVITY
    // May not always be true; lower level of support may still be able to support ISO
    public boolean isSensorSensitivitySupported() {
        return supportedHardwareLevel() == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
    }

    // Lens aperture
    // https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#LENS_APERTURE
    public boolean isLensApertureSupported() {
        try {
            return characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES).length > 1;
        } catch (NullPointerException e) {
            return false;
        }
    }

    // Sensor exposure (shutter speed)
    // https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#SENSOR_EXPOSURE_TIME
    public boolean isShutterSpeedSupported() {
        return supportedHardwareLevel() == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
    }

    // White balance
    // https://developer.android.com/reference/android/hardware/camera2/CaptureRequest#COLOR_CORRECTION_GAINS
    public boolean isWhiteBalanceSupported() {
        return supportedHardwareLevel() == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
    }

    public boolean isMeteringAreaAFSupported() {
        return characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) >= 1;
    }
}
