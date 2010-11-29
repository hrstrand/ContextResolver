/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/pstrand/private/workspace/ActivityGuesser/src/org/peterstrand/service/ISensorService.aidl
 */
package org.peterstrand.service;
public interface ISensorService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.peterstrand.service.ISensorService
{
private static final java.lang.String DESCRIPTOR = "org.peterstrand.service.ISensorService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.peterstrand.service.ISensorService interface,
 * generating a proxy if needed.
 */
public static org.peterstrand.service.ISensorService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.peterstrand.service.ISensorService))) {
return ((org.peterstrand.service.ISensorService)iin);
}
return new org.peterstrand.service.ISensorService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.peterstrand.service.ISensorService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
}
}
}
