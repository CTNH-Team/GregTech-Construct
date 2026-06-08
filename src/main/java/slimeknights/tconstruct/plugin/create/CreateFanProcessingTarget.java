/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;

public interface CreateFanProcessingTarget {

    boolean ctnh$canProcess(FanProcessingType fanProcessingType);

    void ctnh$process(FanProcessingType fanProcessingType, float speed);
}
