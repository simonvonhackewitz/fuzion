# This file is part of the Fuzion language implementation.
#
# The Fuzion language implementation is free software: you can redistribute it
# and/or modify it under the terms of the GNU General Public License as published
# by the Free Software Foundation, version 3 of the License.
#
# The Fuzion language implementation is distributed in the hope that it will be
# useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
# License for more details.
#
# You should have received a copy of the GNU General Public License along with The
# Fuzion language implementation.  If not, see <https://www.gnu.org/licenses/>.


# -----------------------------------------------------------------------
#
#  Tokiwa Software GmbH, Germany
#
#  Source code of Fuzion test Makefile
#
# -----------------------------------------------------------------------



CMD := echo
UTF8_ALIAS := 📢

CMD_PATH := $(shell IFS=:; for dir in $$PATH; do \
    if [ -x "$$dir/$(CMD)" ]; then echo "$$dir/$(CMD)"; exit 0; fi; \
done; echo "not found")

export PATH := $(PATH):$(shell pwd)

all: echo_symlink

echo_symlink:
	ln -s $(CMD_PATH) $(UTF8_ALIAS)
	echo "created symbolic link: $(UTF8_ALIAS) -> $(CMD_PATH)"

override NAME = process_utf8
include ../simple.mk
