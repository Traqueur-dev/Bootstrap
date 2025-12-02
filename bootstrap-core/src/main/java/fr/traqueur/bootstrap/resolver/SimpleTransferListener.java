package fr.traqueur.bootstrap.resolver;

import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * A simple transfer listener that prints download progress to the console.
 * This provides user feedback during dependency resolution.
 */
public class SimpleTransferListener extends AbstractTransferListener {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ROOT));

    @Override
    public void transferStarted(TransferEvent event) {
        TransferResource resource = event.getResource();
        System.out.println("[Download] " + resource.getRepositoryUrl() + resource.getResourceName());
    }

    @Override
    public void transferProgressed(TransferEvent event) {
        TransferResource resource = event.getResource();
        long total = resource.getContentLength();
        long transferred = event.getTransferredBytes();

        if (total > 0) {
            double percentage = (transferred * 100.0) / total;
            System.out.print("\r[Progress] " + formatBytes(transferred) + " / " + formatBytes(total) +
                " (" + FORMAT.format(percentage) + "%)");
        }
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        TransferResource resource = event.getResource();
        long contentLength = event.getTransferredBytes();
        if (contentLength >= 0) {
            System.out.println("\r[Complete] " + resource.getResourceName() + " (" + formatBytes(contentLength) + ")");
        }
    }

    @Override
    public void transferFailed(TransferEvent event) {
        System.err.println("[Failed] " + event.getException().getMessage());
    }

    /**
     * Formats a byte count into a human-readable string.
     *
     * @param bytes the number of bytes
     * @return a formatted string (e.g., "1.5 MB")
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return FORMAT.format(bytes / 1024.0) + " KB";
        } else {
            return FORMAT.format(bytes / (1024.0 * 1024.0)) + " MB";
        }
    }
}