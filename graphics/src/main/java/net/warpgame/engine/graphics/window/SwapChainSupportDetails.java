package net.warpgame.engine.graphics.window;

import net.warpgame.engine.graphics.core.PhysicalDevice;
import net.warpgame.engine.graphics.utility.VulkanAssertionError;
import org.lwjgl.BufferUtils;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * @author MarconZet
 * Created 10.09.2018
 */

public class SwapChainSupportDetails {
    private VkSurfaceCapabilitiesKHR capabilities;
    private VkSurfaceFormatKHR.Buffer formats;
    private IntBuffer presentModes;

    public SwapChainSupportDetails(Window window, PhysicalDevice physicalDevice) {
        acquireSupportDetails(window, physicalDevice);
    }

    private void acquireSupportDetails(Window window, PhysicalDevice physicalDevice) {
        int err;
        long surface = window.getSurface();

        capabilities = VkSurfaceCapabilitiesKHR.create();
        err = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice.get(), surface, capabilities);
        if (err != VK_SUCCESS) {
            throw new VulkanAssertionError("Failed to get physical device surface capabilities", err);
        }

        IntBuffer pFormatCount = BufferUtils.createIntBuffer(1);
        err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.get(), surface, pFormatCount, null);
        if (err != VK_SUCCESS) {
            throw new VulkanAssertionError("Failed to query number of physical device surface formats", err);
        }
        int formatCount = pFormatCount.get(0);

        formats = VkSurfaceFormatKHR.create(formatCount);
        err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice.get(), surface, pFormatCount, formats);
        if (err != VK_SUCCESS) {
            throw new VulkanAssertionError("Failed to query physical device surface formats" , err);
        }

        IntBuffer pPresentModeCount = BufferUtils.createIntBuffer(1);
        err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.get(), surface, pPresentModeCount, null);
        if (err != VK_SUCCESS) {
            throw new VulkanAssertionError("Failed to get number of physical device surface presentation modes", err);
        }
        int presentModeCount = pPresentModeCount.get(0);

        presentModes = BufferUtils.createIntBuffer(presentModeCount);
        err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice.get(), surface, pPresentModeCount, presentModes);
        if (err != VK_SUCCESS) {
            throw new VulkanAssertionError("Failed to get physical device surface presentation modes", err);
        }
    }

    public VkSurfaceFormatKHR chooseSwapSurfaceFormat() {
        if (formats.limit() == 1 && formats.get(0).format() == VK_FORMAT_UNDEFINED) {
            ByteBuffer container = BufferUtils.createByteBuffer(VkSurfaceFormatKHR.SIZEOF);
            container.putInt(VK_FORMAT_B8G8R8A8_UNORM).putInt(VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
            return new  VkSurfaceFormatKHR(container);
        }

        while(formats.hasRemaining()) {
            VkSurfaceFormatKHR availableFormat = formats.get();
            if (availableFormat.format() == VK_FORMAT_B8G8R8A8_UNORM && availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                formats.rewind();
                return availableFormat;
            }
        }
        formats.rewind();
        return formats.get(0);
    }

    public int chooseSwapPresentMode() {
        int bestMode = VK_PRESENT_MODE_FIFO_KHR;
        while(presentModes.hasRemaining()) {
            int presentMode = presentModes.get();
            if (presentMode == VK_PRESENT_MODE_MAILBOX_KHR) {
                bestMode = presentMode;
                break;
            }else if (presentMode == VK_PRESENT_MODE_IMMEDIATE_KHR){
                bestMode = presentMode;
            }
        }
        presentModes.rewind();
        return bestMode;
    }

    public VkExtent2D chooseSwapExtent(Window window) {
        if(capabilities.currentExtent().width() != Integer.MAX_VALUE){
            return VkExtent2D.create().set(capabilities.currentExtent());
        }else {
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            glfwGetFramebufferSize(window.get(), width, height);

            VkExtent2D actualExtent = VkExtent2D.create()
                    .width(width.get())
                    .height(height.get());
            actualExtent.width(Math.max(capabilities.maxImageExtent().width(), Math.min(capabilities.maxImageExtent().width(), actualExtent.width())));
            actualExtent.height(Math.max(capabilities.minImageExtent().height(), Math.min(capabilities.maxImageExtent().height(), actualExtent.height())));
            return actualExtent;
        }
    }

    public VkSurfaceCapabilitiesKHR getCapabilities() {
        return capabilities;
    }

    public VkSurfaceFormatKHR.Buffer getFormats() {
        return formats;
    }

    public IntBuffer getPresentModes() {
        return presentModes;
    }
}