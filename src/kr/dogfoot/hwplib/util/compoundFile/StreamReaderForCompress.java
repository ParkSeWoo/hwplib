package kr.dogfoot.hwplib.util.compoundFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import kr.dogfoot.hwplib.object.fileheader.FileVersion;

import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;

/**
 * ����� ��Ʈ���� �б� ���� ��ü
 * 
 * @author neolord
 */
public class StreamReaderForCompress extends StreamReader {
	/**
	 * ���� Ǯ�� �����͸� �б� ���� InputStream
	 */
	private ByteArrayInputStream bis;

	/**
	 * ������. ����� ��Ʈ���� �о� ������ Ǯ� ���� Ǯ�� �����ͷ� InputStream�� �����.
	 * 
	 * @param de
	 *            ��Ʈ���� ����Ű�� Apache POI ��ü
	 * @param fileVersion
	 * @throws Exception
	 */
	public StreamReaderForCompress(DocumentEntry de, FileVersion fileVersion)
			throws Exception {
		setByteArrayInputStream(de);
		setFileVersion(fileVersion);
	}

	/**
	 * ����� ��Ʈ���� �о� ������ Ǯ� ���� Ǯ�� �����ͷ� InputStream�� �����.
	 * 
	 * @param de
	 *            ��Ʈ���� ����Ű�� Apache POI ��ü
	 * @throws Exception
	 */
	private void setByteArrayInputStream(DocumentEntry de) throws Exception {
		DocumentInputStream dis = new DocumentInputStream(de);
		byte[] compressed = getCompressedBytes(dis, de.getSize() - 8);
		dis.skip(4);
		int originSize = readOriginalSize(dis);
		dis.close();

		byte[] decompressed = decompress(compressed, originSize);
		if (originSize == decompressed.length) {
			bis = new ByteArrayInputStream(decompressed);
			setSize(originSize);
		} else {
			throw new Exception("Decompressed bytes size is wrong.");
		}
	}

	/**
	 * ��Ʈ������ ����� �����͸� �д´�.
	 * 
	 * @param dis
	 *            ��Ʈ���� �б� ���� Apache POI InputStream ��ü
	 * @param size
	 *            ���� ũ��
	 * @return ����� ������
	 * @throws IOException
	 */
	private byte[] getCompressedBytes(DocumentInputStream dis, int size)
			throws IOException {
		byte[] buffer = new byte[size];
		dis.read(buffer);
		return buffer;
	}

	/**
	 * ����� ��Ʈ���� ������ ���� �������� ũ�⸦ �д´�.
	 * 
	 * @param dis
	 *            ��Ʈ���� �б� ���� InputStream ��ü
	 * @return ���� �������� ũ��
	 * @throws IOException
	 */
	private int readOriginalSize(DocumentInputStream dis) throws IOException {
		return dis.readInt();
	}

	/**
	 * ����� �����͸� Ǯ� ���� �����͸� ��´�.
	 * 
	 * @param compressed
	 *            ����� ������
	 * @param originSize
	 *            ���� ������ ũ��
	 * @return ���� ������
	 * @throws DataFormatException
	 * @throws IOException
	 */
	private byte[] decompress(byte[] compressed, int originSize)
			throws DataFormatException, IOException {
		byte[] result = new byte[originSize];
		Inflater decompresser = new Inflater(true);
		decompresser.setInput(compressed, 0, compressed.length);
		decompresser.inflate(result);
		decompresser.end();
		return result;
	}

	@Override
	public void readBytes(byte[] buffer) throws IOException {
		forwardPosition(buffer.length);
		bis.read(buffer);
	}

	@Override
	public byte readSInt1() throws IOException {
		byte[] buffer = readBytes(1);
		return buffer[0];
	}

	/**
	 * n byte�� �о byte �迭���� ��ȯ�Ѵ�.
	 * 
	 * @param n
	 *            ���� ����Ʈ ��
	 * @return ���� ���� byte �迭
	 * @throws IOException
	 */
	private byte[] readBytes(int n) throws IOException {
		byte[] buffer = new byte[n];
		readBytes(buffer);
		return buffer;
	}

	@Override
	public short readSInt2() throws IOException {
		byte[] buffer = readBytes(2);
		return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
				.getShort();
	}

	@Override
	public int readSInt4() throws IOException {
		byte[] buffer = readBytes(4);
		return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	@Override
	public short readUInt1() throws IOException {
		return (short) (readSInt1() & 0xff);
	}

	@Override
	public int readUInt2() throws IOException {
		return readSInt2() & 0xffff;
	}

	@Override
	public long readUInt4() throws IOException {
		return readSInt4() & 0xffffffff;
	}

	@Override
	public double readDouble() throws IOException {
		byte[] buffer = readBytes(8);
		return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
				.getDouble();
	}

	@Override
	public float readFloat() throws IOException {
		byte[] buffer = readBytes(4);
		return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
				.getFloat();
	}

	@Override
	public void skip(long n) throws IOException {
		readBytes((int) n);
	}

	@Override
	public void close() throws IOException {
		bis.close();
		bis = null;
	}
}