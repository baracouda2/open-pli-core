SUMMARY = "Multi boot loader for enigma2"
MAINTAINER = "oe-alliance"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

inherit gitpkgv

SRCREV = "${AUTOREV}"
PV = "1.0+git${SRCPV}"
PKGV = "1.0+git${GITPKGV}"
PR = "r5"
PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "freetype ${@base_contains("BRAND_OEM", "fulan", "fulan-dvb-modules" , "", d)}"

SRC_URI = "git://github.com/oe-alliance/openmultiboot.git;protocol=git \
		file://disable_lcd.patch \		
	"

inherit autotools-brokensep pkgconfig

S = "${WORKDIR}/git"

EXTRA_OEMAKE = " \
    'CFLAGS=${CFLAGS} \
    -I=${includedir}/freetype2 \
    ${@base_contains("MACHINE_FEATURES", "singlecore", "-DOMB_DEFAULT_TIMER=10" , "-DOMB_DEFAULT_TIMER=5", d)} \
    ${@base_contains("MACHINE_FEATURES", "textlcd", "-DOMB_HAVE_TEXTLCD" , "", d)} \
    ${@base_contains("IMAGE_FSTYPES", "ubi", "-DOMB_FLASH_UBI" , "", d)} \
    ${@base_contains("IMAGE_FSTYPES", "jffs2", "-DOMB_FLASH_JFFS2" , "", d)} \
    ${@base_contains("MACHINE_FEATURES", "dreambox", "-DOMB_DREAMBOX", "", d)} \
    -DOMB_KERNEL_MTD=\"/dev/${MTD_KERNEL}\"' \
    'LDFLAGS= -lfreetype ${LDFLAGS}' \
    "

do_install() {
    install -d ${D}/sbin
    install -m 755 ${S}/src/open_multiboot ${D}/sbin
    install -m 644 ${S}/contrib/open-multiboot-branding-helper.py ${D}/sbin
}

pkg_preinst_${PN}() {
#!/bin/sh
if mountpoint -q /usr/lib/enigma2/python/Plugins/Extensions/OpenMultiboot; then
    echo "openMultiBoot will only install on main image."
    echo "Child image is running - canceling installation!"
    sleep 3
    exit 1
else
    echo "Main image is running - proceeding installation..."
    exit 0
fi
}

pkg_postinst_${PN}() {
rm /sbin/init
ln -s /sbin/open_multiboot /sbin/init
}

pkg_postrm_${PN}() {
rm /sbin/init
ln -s /sbin/init.sysvinit /sbin/init
}
