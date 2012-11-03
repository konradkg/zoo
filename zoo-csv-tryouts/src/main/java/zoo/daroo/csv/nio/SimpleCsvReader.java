package zoo.daroo.csv.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SimpleCsvReader<T> {

	private String encoding = System.getProperty("file.encoding");
	private char columnDelimiter = ';';
	private FieldSetMapper<T> fieldSetMapper;
	
	private char[] rowDelimiter = (System.getProperty("line.separator") != null) 
			? System.getProperty("line.separator").toCharArray() : "\r\n".toCharArray();
	
	private int columns = -1;
	private int skipped = 0;
	
	private static int KB = 1024;
	private static int MB = 1024 * KB;
	private static int MaxBufferSize = 50 * MB;
	
	private static int DefaultTokenSize = 255;

	public void read(Path file, FieldResultHandler<T> resultHandler) throws IOException {
		final Path path = file.toRealPath(LinkOption.NOFOLLOW_LINKS);
		final BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
		final int directBufferSize = calculateBufferSize(fileAttributes);
		final Charset charset = Charset.forName(encoding);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(directBufferSize);
		final List<String> tokens = new ArrayList<>();

		CharBuffer tokenBuffer = CharBuffer.allocate(DefaultTokenSize);

		char c;
		final CharsetDecoder charsetDecoder = charset.newDecoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
		final CharactersInterpreter interpreter = new DefaultCharactersInterpreter();
		CharacterType type;
		try (SeekableByteChannel byteChannel = Files.newByteChannel(path, EnumSet.of(StandardOpenOption.READ))) {
			while (byteChannel.read(buffer) > 0) {
				buffer.flip();
				final CharBuffer charBuffer  = charsetDecoder.decode(buffer);

				while (charBuffer.hasRemaining()) {
					c = charBuffer.get();

					type = interpreter.examine(tokenBuffer, c);
					if(type.equals(CharacterType.NEW_ROW)) {
						tokenBuffer.flip();
						tokens.add(tokenBuffer.toString());
						tokenBuffer.clear();
						if(columns == -1 || tokens.size() == columns) {
							try {
								final T result = fieldSetMapper.mapFieldSet(new FieldSet(tokens));
								if(!resultHandler.onResult(result))
									break;
								
							} catch (Exception e) {
								System.err.println("Failed to process row: " + tokens.toString() + ". Error: " +e.getMessage());
								skipped++;
							}
						} else {
							System.err.println("Failed to process row: " + tokens.toString() + ". Invalid columns count" + tokens.size());
							skipped++;
						}
						
						tokens.clear();
					} else if(type.equals(CharacterType.NEW_COLUMN)) {
						tokenBuffer.flip();
						tokens.add(tokenBuffer.toString());
						tokenBuffer.clear();

					} else {
						if (!tokenBuffer.hasRemaining()) {
							final CharBuffer newTokenBuffer = CharBuffer.allocate(tokenBuffer.capacity() + DefaultTokenSize);
							tokenBuffer.flip();
							tokenBuffer = newTokenBuffer.put(tokenBuffer);
						}
						tokenBuffer.put(c);
					}
				}
				//buffer.compact();
				buffer.clear();
			}
		}
	}

	private int calculateBufferSize(BasicFileAttributes fileAttributes) {
		final long fileSize = fileAttributes.size();

		if (fileSize < KB)
			return KB;

		if (fileSize < MaxBufferSize)
			return (int) fileSize;

		return MB;

	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public void setFieldSetMapper(FieldSetMapper<T> fieldSetMapper) {
		this.fieldSetMapper = fieldSetMapper;
	}
	
	public int getSkipped() {
		return skipped;
	}

	public void setColumnDelimiter(char columnDelimiter) {
		this.columnDelimiter = columnDelimiter;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}
	
	public void setRowDelimiter(String rowDelimiter) {
		this.rowDelimiter = rowDelimiter.toCharArray();
	}

	private enum CharacterType {
		NEW_COLUMN, NEW_ROW, NORMAL;
	}
	
	private static interface CharactersInterpreter {
		CharacterType examine(CharBuffer tokenBuffer, char c);
	}
	
	private class DefaultCharactersInterpreter implements CharactersInterpreter{
		private int index = 0;
		@Override
		public CharacterType examine(CharBuffer tokenBuffer, char c) {
			if (c == columnDelimiter) {
				index = 0;
				return CharacterType.NEW_COLUMN;
			}
				
			if(c == rowDelimiter[index]) {
				if(index < rowDelimiter.length - 1 ) {
					index++;
					return CharacterType.NORMAL;
				} else {
					tokenBuffer.position(tokenBuffer.position() - index);
					index = 0;
					return CharacterType.NEW_ROW;
				}
			}
				
			index = 0;
			return CharacterType.NORMAL;
		}
		
	}
	
	//---------------------------------------------------------------------
	//TEMP
	public static void println(List<String> tokens) {
		System.out.println("size: " + tokens.size() + " " + tokens.toString());
	}
	
	public static void main(String[] args) throws Exception {
		SimpleCsvReader<List<String>> csvReader = new SimpleCsvReader<>();
		csvReader.setEncoding("UTF-8");
		csvReader.setFieldSetMapper(new FieldSetMapper<List<String>>() {
			@Override
			public List<String> mapFieldSet(FieldSet fieldSet) {
				final List<String> result = new ArrayList<>();
				for(int i = 0; i < fieldSet.size(); i++) {
					result.add(fieldSet.readString(i));
				}
				return result;
			}
		});
		
		long start = System.nanoTime();
		csvReader.read(Paths.get("p:/Temp/h2_data/dump_lite2.csv_big"), new FieldResultHandler<List<String>>() {
			@Override
			public boolean onResult(List<String> result) {
				println(result);
				return true;
			}
		});
		System.out.println("Exec time: " + TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS) + " [ms].");
	}
}
