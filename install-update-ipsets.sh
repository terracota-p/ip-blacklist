#!/bin/sh

apt-get update
# As per http://firehol.org/installing/debian/
# Needed, or several libs need to be installed explicityli (eg: apt-get install -y iproute2 kmod iputils-ping inetutils-traceroute ...)
apt-get install -y firehol
# As per https://github.com/firehol/blocklist-ipsets/wiki/installing-update-ipsets
apt-get install -y zlib1g-dev gcc make git autoconf autogen automake pkg-config curl ipset

# cd somewhere
mkdir -p /opt/update-ipsets/
cd /opt/update-ipsets/

# download iprange and firehol from github
git clone https://github.com/firehol/iprange.git iprange.git
git clone https://github.com/firehol/firehol.git firehol.git

# install iprange
cd iprange.git

./autogen.sh
# make sure it completed successfully

./configure --prefix=/usr CFLAGS="-march=native -O3" --disable-man
# make sure it completed successfully

make
# make sure it completed successfully

make install
# make sure it completed successfully

# install firehol
cd ../firehol.git

./autogen.sh
# make sure it completed successfully

./configure --prefix=/usr --sysconfdir=/etc --disable-man --disable-doc
# make sure it completed successfully

make
# make sure it completed successfully

make install
# make sure it completed successfully

# Create the default RUN_PARENT_DIR='/usr/var/run' who is set in '/etc/firehol/update-ipsets.conf'
mkdir -p /usr/var/run


# TODO Enable lists needed by firehol_level1 and firehol_level2 for update-ipsets
cat >>/etc/firehol/update-ipsets.conf <<EOF
BASE_DIR="/opt/blocklist-ipsets"
EOF
update-ipsets enable feodo palevo sslbl zeus zeus_badips dshield spamhaus_drop spamhaus_edrop fullbogons firehol_level1 bambenek_c2 ransomware_rw firehol_level2 blocklist_de greensnow
