package net.tokyolancer.lang.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class AsyncURL {

    // I advise you not to increase the value of the MAX_CHUNKS due to some 'unknown' reasons
    // that can happen due to async download. Firstly, the download can be MUCH slower than strict download.
    private static final int MAX_CHUNKS = 4;

    private final ExecutorService SERVICE = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors() );

    private final List<Future<byte[]>> tasks = new ArrayList<>();

    private final URL url;
    private final int contentLength;

    public AsyncURL(URL url) throws IOException {
        // TODO: find another way to fetch content length faster
        this(url, url.openConnection().getContentLength() );
    }

    public AsyncURL(URL url, int contentLength) {
        this.url = url;
        this.contentLength = contentLength;
    }

    public byte[] readAllBytes() throws InterruptedException, ExecutionException {
        // define default size per chunk
        final int chunkSize = contentLength / MAX_CHUNKS;
        // allocate the resulted byte-array
        final byte[] result = new byte[contentLength];

        int currChunkSize = 0;
        // distributes byte downloads between chunks
        for (int i = 0; i < MAX_CHUNKS - 1; i++) {
            ByteTransfer bytesV2 = new ByteTransfer(url, currChunkSize, (currChunkSize += chunkSize) - 1);
            this.tasks.add(SERVICE.submit(() -> {
                bytesV2.transfer(result);
                return null;
            } ) );
        }
        // last task will take the remaining bytes
        ByteTransfer bytesV2 = new ByteTransfer(url, currChunkSize, contentLength);
        this.tasks.add(SERVICE.submit(() -> {
            bytesV2.transfer(result);
            return null;
        } ) );

        // wait until all tasks are done
        for (Future<?> future : this.tasks) future.get();

        return result;
    }

    @Deprecated
    public synchronized byte[] readAllBytesAsync() throws ExecutionException, InterruptedException {
        final int maxChunkSize = contentLength / MAX_CHUNKS;

        int currChunkSize = 0;
        // distributes byte downloads between chunks
        for (int i = 0; i < MAX_CHUNKS - 1; i++) {
            tasks.add(SERVICE.submit(new RangedBytes(url, currChunkSize, (currChunkSize += maxChunkSize) - 1) ) );
        }
        // last task will take the remaining bytes
        tasks.add(SERVICE.submit(new RangedBytes(url, currChunkSize, contentLength)) );

        byte[] result = new byte[contentLength];

        // the worst thing I have had ever done
        ByteBuffer target = ByteBuffer.wrap(result);
        for (Future<byte[]> task : tasks)
            target.put(task.get() );

        return result;
    }

    private static class ByteTransfer {

        private final URL url;
        private final int range0;
        private final int range1;

        ByteTransfer(URL url, int range0, int range1) {
            this.url = url;
            this.range0 = range0;
            this.range1 = range1;
        }

        public void transfer(byte[] array) throws IOException {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Range", String.format("bytes=%s-%s", range0, range1) );
            InputStream inStream = connection.getInputStream();

            int nextIndex = range0;
            int currByte;
            while ((currByte = inStream.read() ) != -1 && nextIndex <= range1)
                array[nextIndex++] = (byte) currByte;

            // try { super.finalize(); } catch (Throwable ignored) { }
        }
    }

    @Deprecated
    private static class RangedBytes implements Callable<byte[]> {

        private final URL url;
        private final int range0;
        private final int range1;

        RangedBytes(URL url, int range0, int range1) {
            this.url = url;
            this.range0 = range0;
            this.range1 = range1;
        }

        @Override
        public byte[] call() throws Exception {
            System.out.println("Received range: " + range0 + " --> " + range1);
            System.out.println("Max range:" + (range1 - range0) );
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            connection.setRequestProperty("Range", String.format("bytes=%s-%s", range0, range1) );
            connection.setRequestProperty("Content-Range", String.format("bytes=%s-%s", range0, range1) );
            return NetUtil.toByteArray(connection.getInputStream() );
        }
    }
}
