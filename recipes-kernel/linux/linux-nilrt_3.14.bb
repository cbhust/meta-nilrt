DESCRIPTION = "Linux kernel based on nilrt branch"

inherit kernel
require recipes-kernel/linux/linux-yocto.inc

# The NI kernel build includes on-target versioning tools that
# link against the gcc provided runtime
do_kernel_configme[depends] += "libgcc:do_populate_sysroot"

SRC_URI = "git://git.amer.corp.natinst.com/linux.git;protocol=git;nocheckout=1;branch=nilrt/15.0/3.14"

LINUX_VERSION ?= "3.14"
LINUX_VERSION_EXTENSION ?= "-nilrt"

# A defconfig must be provided to trigger OE to use a custom kernel config during patch.
# As the desired config is stored in-tree with the kernel source, it not available until
# after kernel_checkout, so point to an empty defconfig to start.
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "file://defconfig"
KCONFIG_MODE="--alldefconfig"

SRCREV="${AUTOREV}"
PV = "${LINUX_VERSION}+git${SRCPV}"

# Overwrite the stub defconfig with the desired kernel config once available after kernel_checkout
addtask kernel_myconfig before do_patch after do_kernel_checkout

do_kernel_myconfig() {
if [ "${TARGET_ARCH}" = "x86_64" ]; then
    MYCONFIG="linux/arch/x86/configs/nati_x86_64_defconfig"
elif [ "${TARGET_ARCH}" = "arm" ]; then
    MYCONFIG="linux/arch/arm/configs/nati_zynq_defconfig"
else
    bberror "Could not set MYCONFIG for ${TARGET_ARCH}"
    exit 1
fi
    cp ${WORKDIR}/${MYCONFIG} ${THISDIR}/files/defconfig
}

# defconfig is consumed in patch task and .config file is used thereafter.
# We can therefore restore defconfig back to its default (blank) state now.
do_patch_append() {
    cat /dev/null > ${THISDIR}/files/defconfig
}