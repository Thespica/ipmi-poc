/*
 * Copyright (c) Nextian. All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 *
 */
package com.nextian.ipmi.coding.commands.chassis;

import com.nextian.ipmi.coding.commands.ResponseData;
import com.nextian.ipmi.common.TypeConverter;

/**
 * Get Chassis Status response.
 */
public class GetChassisStatusResponseData implements ResponseData {

    private static final String FRONT_PANEL_BUTTON_CAPABILITIES_NOT_SET = "Front Panel Button Capabilities not set";
    private byte currentPowerState;
    private byte lastPowerEvent;
    private byte miscChassisState;
    private boolean isFrontPanelButtonCapabilitiesSet;
    private byte frontPanelButtonCapabilities;

    public GetChassisStatusResponseData() {
        setFrontPanelButtonCapabilitiesSet(false);
    }

    public byte getCurrentPowerState() {
        return currentPowerState;
    }

    public void setCurrentPowerState(byte currentPowerState) {
        this.currentPowerState = currentPowerState;
    }

    /**
     * Convert received power state value to its appropriate power policy enumeration
     *
     * @return power policy value
     * @throws IllegalArgumentException when provided policy is out of range and does not match any defined enum value
     */
    public PowerRestorePolicy getPowerRestorePolicy() {
        switch ((currentPowerState & TypeConverter.intToByte(0x60)) >> 5) {
            case 0:
                return PowerRestorePolicy.PoweredOff;
            case 1:
                return PowerRestorePolicy.PowerRestored;
            case 2:
                return PowerRestorePolicy.PoweredUp;
            default:
                throw new IllegalArgumentException("Invalid Power Restore Policy");
        }
    }

    /**
     * Checks if {@link #currentPowerState} indicates a power control fault.
     *
     * @return true when controller attempted to turn system power on or off,
     * but system did not enter desired state
     */
    public boolean isPowerControlFault() {
        return ((currentPowerState & TypeConverter.intToByte(0x10)) != 0);
    }

    /**
     * Checks if {@link #currentPowerState} indicates a power fault.
     *
     * @return true when fault was detected in main power subsystem
     */
    public boolean isPowerFault() {
        return ((currentPowerState & TypeConverter.intToByte(0x8)) != 0);
    }

    /**
     * Checks if {@link #currentPowerState} indicates an interlock.
     *
     * @return true when interlock was detected (chassis is presently shut down
     * because a chassis panel interlock switch is active)
     */
    public boolean isInterlock() {
        return ((currentPowerState & TypeConverter.intToByte(0x4)) != 0);
    }

    /**
     * Checks if {@link #currentPowerState} indicates a power overload.
     *
     * @return true when system was shut down because of power overload condition
     */
    public boolean isPowerOverload() {
        return ((currentPowerState & TypeConverter.intToByte(0x2)) != 0);
    }

    /**
     * Checks if {@link #currentPowerState} indicates a power on state.
     *
     * @return true when system power is on
     */
    public boolean isPowerOn() {
        return ((currentPowerState & TypeConverter.intToByte(0x1)) != 0);
    }

    public byte getLastPowerEvent() {
        return lastPowerEvent;
    }

    public void setLastPowerEvent(byte lastPowerEvent) {
        this.lastPowerEvent = lastPowerEvent;
    }

    /**
     * Check if the last power on was triggered by IPMI command.
     *
     * @return true when last power on was triggered via IPMI command
     */
    public boolean wasIpmiPowerOn() {
        return ((lastPowerEvent & TypeConverter.intToByte(0x10)) != 0);
    }

    /**
     * Check if the last power down was caused by a power fault.
     *
     * @return true if last power off was caused by a power fault
     */
    public boolean wasPowerFault() {
        return ((lastPowerEvent & TypeConverter.intToByte(0x8)) != 0);
    }

    /**
     * Check if the last power down was caused by a power interlock.
     *
     * @return true if last power down was caused by a power interlock being activated
     */
    public boolean wasInterlock() {
        return ((lastPowerEvent & TypeConverter.intToByte(0x4)) != 0);
    }

    /**
     * Check if the last power down was caused by an overload.
     *
     * @return true if last power down was caused by a Power overload
     */
    public boolean wasPowerOverload() {
        return ((lastPowerEvent & TypeConverter.intToByte(0x2)) != 0);
    }

    /**
     * Check if the last power down was caused by an AC failure.
     *
     * @return true if AC failed
     */
    public boolean acFailed() {
        return ((lastPowerEvent & TypeConverter.intToByte(0x1)) != 0);
    }

    /**
     * Get  Misc Chassis State code.
     *
     * @return raw value od Misc Chassis State field
     */
    public byte getMiscChassisState() {
        return miscChassisState;
    }

    /**
     * Set Misc Chassis State code.
     *
     * @param miscChassisState raw value od Misc Chassis State field
     */
    public void setMiscChassisState(byte miscChassisState) {
        this.miscChassisState = miscChassisState;
    }

    /**
     * Check if Chassis Identify command and state info are supported.
     *
     * @return true if Chassis Identify command and state info are supported
     */
    public boolean isChassisIdentifyCommandSupported() {
        return ((miscChassisState & TypeConverter.intToByte(0x40)) != 0);
    }

    /**
     * Get Chassis Identify state info.
     *
     * @return chassis identify state
     * @throws IllegalAccessError when Chassis Identify command and state info are supported
     */
    public ChassisIdentifyState getChassisIdentifyState() throws IllegalAccessError {
        if (!isChassisIdentifyCommandSupported()) {
            throw new IllegalAccessError("Chassis Identify command and state not supported");
        }
        return ChassisIdentifyState.parseInt((miscChassisState & TypeConverter.intToByte(0x30)) >> 4);
    }

    /**
     * Check if cooling or fan fault was detected.
     *
     * @return true if cooling or fan fault was detected
     */
    public boolean coolingFaultDetected() {
        return ((miscChassisState & TypeConverter.intToByte(0x8)) != 0);
    }

    /**
     * Check if if drive fault was detected.
     *
     * @return true if drive fault was detected
     */
    public boolean driveFaultDetected() {
        return ((miscChassisState & TypeConverter.intToByte(0x4)) != 0);
    }

    /**
     * Check if Front Panel Lockout is active (power off and reset via chassis push-buttons is disabled).
     *
     * @return true if lockout is active
     */
    public boolean isFrontPanelLockoutActive() {
        return ((miscChassisState & TypeConverter.intToByte(0x2)) != 0);
    }

    /**
     * Check if Chassis Intrusion is active.
     *
     * @return true if Chassis intrusion is active.
     */
    public boolean isChassisIntrusionActive() {
        return ((miscChassisState & TypeConverter.intToByte(0x1)) != 0);
    }

    /**
     * Get Front Panel Button Capabilities code.
     *
     * @return raw value od Front Panel Button Capabilities field
     */
    public byte getFrontPanelButtonCapabilities() {
        return frontPanelButtonCapabilities;
    }

    /**
     * Set Front Panel Button Capabilities code.
     *
     * @param frontPanelButtonCapabilities raw value od Front Panel Button Capabilities
     */
    public void setFrontPanelButtonCapabilities(byte frontPanelButtonCapabilities) {
        this.frontPanelButtonCapabilities = frontPanelButtonCapabilities;
        setFrontPanelButtonCapabilitiesSet(true);
    }

    /**
     * Check if Standby (sleep) button disabling is allowed.
     *
     * @return true if disabling is allowed
     * @throws IllegalAccessException when Front Panel Button Capabilities have not been set
     */
    public boolean isStandbyButtonDisableAllowed() throws IllegalAccessException {
        if (!isFrontPanelButtonCapabilitiesSet()) {
            throw new IllegalAccessException(FRONT_PANEL_BUTTON_CAPABILITIES_NOT_SET);
        }
        return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x80)) != 0);
    }

    /**
     * Check if Diagnostic Interrupt button disabling is allowed.
     *
     * @return true if true if disabling is allowed
     * @throws IllegalAccessException when Front Panel Button Capabilities have not been set
     */
    public boolean isDiagnosticInterruptButtonDisableAllowed() throws IllegalAccessException {
        if (!isFrontPanelButtonCapabilitiesSet()) {
            throw new IllegalAccessException(FRONT_PANEL_BUTTON_CAPABILITIES_NOT_SET);
        }
        return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x40)) != 0);
    }

    /**
     * Check if Reset button disabling is allowed
     *
     * @return true if disabling is allowed
     * @throws IllegalAccessException when Front Panel Button Capabilities have not been set
     */
    public boolean isResetButtonDisableAllowed() throws IllegalAccessException {
        if (!isFrontPanelButtonCapabilitiesSet()) {
            throw new IllegalAccessException(FRONT_PANEL_BUTTON_CAPABILITIES_NOT_SET);
        }
        return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x20)) != 0);
    }

    /**
     * Check if Power off button disabling is allowed (in the case there is a single, combined power/standby (sleep)
     * button: disabling power off also disables sleep requests).
     *
     * @return true if disabling is allowed
     * @throws IllegalAccessException when Front Panel Button Capabilities have not been set
     */
    public boolean isPowerOffButtonDisableAllowed() throws IllegalAccessException {
        if (!isFrontPanelButtonCapabilitiesSet()) {
            throw new IllegalAccessException(FRONT_PANEL_BUTTON_CAPABILITIES_NOT_SET);
        }
        return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x10)) != 0);
    }

    /**
     * Check if Standby/sleep button disabling is allowed.
     *
     * @return true if disabling is allowed
     * @throws IllegalAccessException when Front Panel Button Capabilities have not been set
     */
    public boolean isStandbyButtonDisabled() throws IllegalAccessException {
        if (!isFrontPanelButtonCapabilitiesSet()) {
            throw new IllegalAccessException(FRONT_PANEL_BUTTON_CAPABILITIES_NOT_SET);
        }
        return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x8)) != 0);
    }

    /**
     * Check if Diagnostic Interrupt button has been disabled.
     *
     * @return true if the button is disabled
     * @throws IllegalAccessException when Front Panel Button Capabilities have not been set
     */
    public boolean isDiagnosticInterruptButtonDisabled() throws IllegalAccessException {
        if (!isFrontPanelButtonCapabilitiesSet()) {
            throw new IllegalAccessException(FRONT_PANEL_BUTTON_CAPABILITIES_NOT_SET);
        }
        return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x4)) != 0);
    }

    /**
     * Check if Reset button has been disabled.
     *
     * @return true if the button is disabled
     * @throws IllegalAccessException when Front Panel Button Capabilities have not been set
     */
    public boolean isResetButtonDisabled() throws IllegalAccessException {
        if (!isFrontPanelButtonCapabilitiesSet()) {
            throw new IllegalAccessException(FRONT_PANEL_BUTTON_CAPABILITIES_NOT_SET);
        }
        return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x2)) != 0);
    }

    /**
     * Check if Power off button has been disabled (in the case there is a single combined
     * power/standby (sleep) button, disabling power off also disables sleep requests via that button).
     *
     * @return if the button is disabled
     * @throws IllegalAccessException when Front Panel Button Capabilities have not been set
     */
    public boolean isPowerOffButtonDisabled() throws IllegalAccessException {
        if (!isFrontPanelButtonCapabilitiesSet()) {
            throw new IllegalAccessException(FRONT_PANEL_BUTTON_CAPABILITIES_NOT_SET);
        }
        return ((frontPanelButtonCapabilities & TypeConverter.intToByte(0x1)) != 0);
    }

    /**
     * Check if Front Panel Button Capabilities optional value is set.
     *
     * @return true if Front Panel Button Capabilities value is set
     */
    public boolean isFrontPanelButtonCapabilitiesSet() {
        return isFrontPanelButtonCapabilitiesSet;
    }

    /**
     * Set flag specifying that Front Panel Button Capabilities value is set.
     *
     * @param isFrontPanelButtonCapabilitiesSet true means that the Front Panel Button Capabilities value is set
     */
    private void setFrontPanelButtonCapabilitiesSet(boolean isFrontPanelButtonCapabilitiesSet) {
        this.isFrontPanelButtonCapabilitiesSet = isFrontPanelButtonCapabilitiesSet;
    }
}
