package io.flutter.plugins.camera;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugins.camera.CameraPermissions.PermissionsRegistry;
import io.flutter.view.TextureRegistry;

final class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler {
    private final Activity activity;
    private final BinaryMessenger messenger;
    private final CameraPermissions cameraPermissions;
    private final PermissionsRegistry permissionsRegistry;
    private final TextureRegistry textureRegistry;
    private final MethodChannel methodChannel;
    private final EventChannel imageStreamChannel;
    private @Nullable
    Camera camera;

    MethodCallHandlerImpl(
            Activity activity,
            BinaryMessenger messenger,
            CameraPermissions cameraPermissions,
            PermissionsRegistry permissionsAdder,
            TextureRegistry textureRegistry) {
        this.activity = activity;
        this.messenger = messenger;
        this.cameraPermissions = cameraPermissions;
        this.permissionsRegistry = permissionsAdder;
        this.textureRegistry = textureRegistry;

        methodChannel = new MethodChannel(messenger, "plugins.flutter.io/camera");
        imageStreamChannel = new EventChannel(messenger, "plugins.flutter.io/camera/imageStream");
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
        switch (call.method) {
            case "availableCameras":
                try {
                    result.success(CameraUtils.getAvailableCameras(activity));
                } catch (Exception e) {
                    handleException(e, result);
                }
                break;
            case "initialize": {

                //TODO: init with more config
                if (camera != null) {
                    camera.close();
                }
                cameraPermissions.requestPermissions(
                        activity,
                        permissionsRegistry,
                        call.argument("enableAudio"),
                        (String errCode, String errDesc) -> {
                            if (errCode == null) {
                                try {
                                    instantiateCamera(call, result);
                                } catch (Exception e) {
                                    handleException(e, result);
                                }
                            } else {
                                result.error(errCode, errDesc, null);
                            }
                        });

                break;
            }
            case "takePicture": {
                camera.takePicture(call.argument("path"), result);
                break;
            }
            case "setPointOfInterest": {
                //result.notImplemented();

                try {
                    camera.focusToPoint(call.argument("offsetX"), call.argument("offsetY"));
                } catch (CameraAccessException e) {
                    handleException(e, result);
                }
                break;
            }
            case "zoom": {
                try {
                    // Always convert the number to float since it can be int/double
                    camera.zoom(call.argument("step"));
                    result.success(null);
                } catch (CameraAccessException e) {
                    result.error("CameraAccess", e.getMessage(), null);
                }
                break;
            }
            case "prepareForVideoRecording": {
                // This optimization is not required for Android.
                result.success(null);
                break;
            }
            case "startVideoRecording": {
                camera.startVideoRecording(call.argument("filePath"), result);
                break;
            }
            case "stopVideoRecording": {
                camera.stopVideoRecording(result);
                break;
            }
            case "pauseVideoRecording": {
                camera.pauseVideoRecording(result);
                break;
            }
            case "resumeVideoRecording": {
                camera.resumeVideoRecording(result);
                break;
            }
            case "startImageStream": {
                try {
                    camera.startPreviewWithImageStream(imageStreamChannel);
                    result.success(null);
                } catch (Exception e) {
                    handleException(e, result);
                }
                break;
            }
            case "stopImageStream": {
                try {
                    camera.startPreview();
                    result.success(null);
                } catch (Exception e) {
                    handleException(e, result);
                }
                break;
            }

            case "setAutoFocus": {
                try {
                    camera.setAutoFocus((boolean) call.argument("autoFocusValue"));
                    result.success(null);
                } catch (Exception e) {
                    handleException(e, result);
                }
                break;
            }
            case "setFlashMode": {
                try {
                    Log.d("TAG", call.argument("flashMode").toString());
                    camera.setFlash((int) call.argument("flashMode"));
                    result.success(null);
                } catch (Exception e) {
                    handleException(e, result);
                }
                break;
            }
            case "hasFlash": {
                if (camera != null) {
                    result.success(camera.hasFlash());
                } else {
                    result.success(false);
                }
                break;
            }
            case "supportSensorSensitivity":
                result.success(camera.getCameraCompatibility().isSensorSensitivitySupported());
                break;
            case "supportLensAperture":
                result.success(camera.getCameraCompatibility().isLensApertureSupported());
                break;
            case "supportShutterSpeed":
                result.success(camera.getCameraCompatibility().isShutterSpeedSupported());
                break;
            case "supportWhiteBalance":
                result.success(camera.getCameraCompatibility().isWhiteBalanceSupported());
                break;
            case "setSensorSensitivity":
                try {
                    Object sensorSensitivity = call.argument("sensorSensitivity");
                    camera.setSensorSensitivity(sensorSensitivity == null ? null : ((Number) sensorSensitivity).intValue());
                    result.success(null);
                } catch (CameraAccessException e) {
                    handleException(e, result);
                }
                break;
            case "setLensAperture":
                try {
                    Object lensAperture = call.argument("lensAperture");
                    camera.setLensAperture(lensAperture == null ? null : ((Number) lensAperture).floatValue());
                    result.success(null);
                } catch (CameraAccessException e) {
                    handleException(e, result);
                }
                break;
            case "setSensorExposure":
                try {
                    Object sensorExposure = call.argument("sensorExposure");
                    camera.setSensorExposure(sensorExposure == null ? null : ((Number) sensorExposure).longValue());
                    result.success(null);
                } catch (CameraAccessException e) {
                    handleException(e, result);
                }
                break;
            case "setWhiteBalanceGain":
                try {
                    Object whiteBalance = call.argument("whiteBalance");
                    camera.setWhiteBalanceGain(whiteBalance == null ? null : ((Number) whiteBalance).intValue());
                    result.success(null);
                } catch (CameraAccessException e) {
                    handleException(e, result);
                }
                break;
            case "dispose": {
                if (camera != null) {
                    camera.dispose();
                }
                result.success(null);
                break;
            }
            default:
                result.notImplemented();
                break;
        }
    }

    void stopListening() {
        methodChannel.setMethodCallHandler(null);
    }

    private boolean hasFlash() {
        return activity
                .getApplicationContext()
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void instantiateCamera(MethodCall call, Result result) throws CameraAccessException {
        String cameraName = call.argument("cameraName");
        String resolutionPreset = call.argument("resolutionPreset");
        boolean enableAudio = call.argument("enableAudio");
        boolean autoFocusEnabled = call.argument("autoFocusEnabled");
        boolean enableAutoExposure = call.argument("enableAutoExposure");
        int flashMode = call.argument("flashMode");

        TextureRegistry.SurfaceTextureEntry flutterSurfaceTexture =
                textureRegistry.createSurfaceTexture();
        DartMessenger dartMessenger = new DartMessenger(messenger, flutterSurfaceTexture.id());
        camera =
                new Camera(
                        activity,
                        flutterSurfaceTexture,
                        dartMessenger,
                        cameraName,
                        resolutionPreset,
                        enableAudio,
                        autoFocusEnabled,
                        enableAutoExposure,
                        flashMode);

        camera.open(result);
    }

    // We move catching CameraAccessException out of onMethodCall because it causes a crash
    // on plugin registration for sdks incompatible with Camera2 (< 21). We want this plugin to
    // to be able to compile with <21 sdks for apps that want the camera and support earlier version.
    @SuppressWarnings("ConstantConditions")
    private void handleException(Exception exception, Result result) {
        if (exception instanceof CameraAccessException) {
            result.error("CameraAccess", exception.getMessage(), null);
        }

        throw (RuntimeException) exception;
    }
}
