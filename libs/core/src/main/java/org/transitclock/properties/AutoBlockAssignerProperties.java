package org.transitclock.properties;

import lombok.Data;

@Data
public class AutoBlockAssignerProperties {
    // config param: transitclock.autoBlockAssigner.autoAssignerEnabled
    // Set to true to enable the auto assignment feature where the system tries to assign vehicle to an available block
    private boolean autoAssignerEnabled = false;

    // config param: transitclock.autoBlockAssigner.ignoreAvlAssignments
    // For when want to test automatic assignments. When set to true then system ignores assignments from AVL feed so vehicles need to be automatically assigned instead
    private boolean ignoreAvlAssignments = false;

    // config param: transitclock.autoBlockAssigner.minDistanceFromCurrentReport
    // AutoBlockAssigner looks at two AVL reports to match vehicle. This parameter specifies how far away those AVL reports need to be sure that the vehicle really is moving and in service. If getting incorrect matches then this value should likely be increased.
    private Double minDistanceFromCurrentReport = 100.0;

    // config param: transitclock.autoBlockAssigner.allowableEarlySeconds
    // How early a vehicle can be in seconds and still be automatically assigned to a block
    private Integer allowableEarlySeconds = 180;

    // config param: transitclock.autoBlockAssigner.allowableLateSeconds
    // How late a vehicle can be in seconds and still be automatically assigned to a block
    private Integer allowableLateSeconds = 300;

    // config param: transitclock.autoBlockAssigner.minTimeBetweenAutoAssigningSecs
    // Minimum time per vehicle that can do auto assigning. Auto assigning is computationally expensive, especially when there are many blocks. Don't need to do it that frequently. Especially important for agencies with high reporting rates. So this param allows one to limit how frequently auto assigner called for vehicle
    private Integer minTimeBetweenAutoAssigningSecs = 30;

}
