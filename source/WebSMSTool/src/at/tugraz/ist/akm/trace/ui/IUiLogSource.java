package at.tugraz.ist.akm.trace.ui;


public interface IUiLogSource
{
    public void registerUiLogSink(IUiLogSink sink);
    public void unregisterUiLogSink();
}
