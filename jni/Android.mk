
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

os = LINUX

#arch = IA32


LOCAL_CFLAGS = -fPIC -Wall -Wextra -D$(os) -finline-functions -O3 -fno-strict-aliasing -fexceptions

ifeq ($(arch), IA32)
   LOCAL_CFLAGS += -DIA32
endif

ifeq ($(arch), POWERPC)
   LOCAL_CFLAGS += -mcpu=powerpc
endif

ifeq ($(arch), SPARC)
   LOCAL_CFLAGS += -mcpu=sparc
endif

ifeq ($(arch), IA64)
   LOCAL_CFLAGS += -DIA64
endif

ifeq ($(arch), AMD64)
   LOCAL_CFLAGS += -DAMD64
endif



LOCAL_MODULE    := UDTBarchart
LOCAL_SRC_FILES := md5.cpp common.cpp window.cpp list.cpp buffer.cpp \
				   packet.cpp channel.cpp queue.cpp ccc.cpp cache.cpp core.cpp epoll.cpp \
				   api.cpp com_barchart_udt_SocketUDT.cpp

LOCAL_CPP_EXTENSION := .cpp
LOCAL_LDLIBS := -llog




include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_STATIC_LIBRARY)



#C++ = g++ 


#CCFLAGS = -fPIC -Wall -Wextra -D$(os) -finline-functions -O3 -fno-strict-aliasing #-msse3
#
#
#OBJS = md5.o common.o window.o list.o buffer.o packet.o channel.o queue.o ccc.o cache.o core.o epoll.o api.o
#DIR = $(shell pwd)
#
#all: libudt.so libudt.a udt
#
#%.o: %.cpp %.h udt.h
#	$(C++) $(CCFLAGS) $< -c
#
#libudt.so: $(OBJS)
#ifneq ($(os), OSX)
#	$(C++) -shared -o $@ $^
#else
#	$(C++) -dynamiclib -o libudt.dylib -lstdc++ -lpthread -lm $^
#endif
#
#libudt.a: $(OBJS)
#	ar -rcs $@ $^
#
#udt:
#	cp udt.h udt
#
#clean:
#	rm -f *.o *.so *.dylib *.a udt
#
#install:
#	export LD_LIBRARY_PATH=$(DIR):$$LD_LIBRARY_PATH
