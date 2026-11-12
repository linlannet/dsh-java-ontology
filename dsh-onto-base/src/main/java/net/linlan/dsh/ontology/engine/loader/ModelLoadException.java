package net.linlan.dsh.ontology.engine.loader;

/** 模型加载失败：携带文件、节点路径、行号。 */
public class ModelLoadException extends RuntimeException {

    private final String file;
    private final String nodePath;
    private final int line;

    public ModelLoadException(String message) {
        this(message, null, null, -1, null);
    }

    public ModelLoadException(String message, String file, String nodePath, int line) {
        this(message, file, nodePath, line, null);
    }

    public ModelLoadException(String message, String file, String nodePath, int line, Throwable cause) {
        super(buildMessage(message, file, nodePath, line), cause);
        this.file = file;
        this.nodePath = nodePath;
        this.line = line;
    }

    public String file() {
        return file;
    }

    public String nodePath() {
        return nodePath;
    }

    public int line() {
        return line;
    }

    private static String buildMessage(String message, String file, String path, int line) {
        StringBuilder sb = new StringBuilder(message);
        if (file != null) {
            sb.append(" [file=").append(file);
            if (path != null) {
                sb.append(", path=").append(path);
            }
            if (line > 0) {
                sb.append(", line=").append(line);
            }
            sb.append(']');
        }
        return sb.toString();
    }
}
