package cap4j.session;

import cap4j.GlobalContext;
import cap4j.scm.BaseScm;
import cap4j.scm.SvnScm;
import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * User: ACHASCHEV
 * Date: 7/23/13
 */
public class GenericUnixLocalEnvironment extends SystemEnvironment {
    private static final Logger logger = LoggerFactory.getLogger(GenericUnixLocalEnvironment.class);

    public GenericUnixLocalEnvironment(String name) {
        super(name);
    }

    @Override
    public List<String> ls(String path) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.ls");
    }

    /**
     * Single files a treated in a different way.
     * @param dest
     * @param paths
     */
    @Override
    public void zip(String dest, Collection<String> paths) {
        final Zip zip = new Zip();

        zip.setCompress(false);
        zip.setDestFile(new File(dest));
        zip.setProject(new Project());

        if(paths.size() != 1){
        for (String path : paths) {
            final ZipFileSet set = new ZipFileSet();

            final File file = new File(path);

            set.setDir(file.getParentFile());
            set.setIncludes(file.getName());

            zip.addZipfileset(set);
        }
        }else{
            final ZipFileSet set = new ZipFileSet();

            final File toAdd = new File(paths.iterator().next());

            if(toAdd.isDirectory()){
                set.setDir(toAdd);
            }else{
                set.setFile(toAdd);
            }

            zip.addZipfileset(set);
        }

        zip.execute();
    }

    @Override
    public void unzip(String file, @Nullable String destDir) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.unzip");
    }

    @Override
    public String newTempDir() {
        final File cap4jDir = new File(FileUtils.getTempDirectory(), "cap4j");

        if(cap4jDir.exists()){
            try {
                FileUtils.deleteDirectory(cap4jDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Preconditions.checkArgument(cap4jDir.mkdir(), "could not create temp dir");

        return cap4jDir.getAbsolutePath();
    }

    @Override
    public boolean isUnix() {
        return true;
    }

    @Override
    public boolean isNativeUnix() {
        return SystemUtils.IS_OS_UNIX;
    }

    public static class ProcessRunner<T extends SvnScm.CommandLineResult> {
        BaseScm.CommandLine<T> line;

        int processTimeoutMs = 60000;

        public ProcessRunner(BaseScm.CommandLine<T> line) {
            this.line = line;
        }

        public static class ProcessResult {
            public int exitCode;
            public String text;

            public ProcessResult(int exitCode, String text) {
                this.exitCode = exitCode;
                this.text = text;
            }
        }

        public ProcessResult run() {
            Process process = null;
            final StringBuilder sb = new StringBuilder(1024);

            try {
                process = new ProcessBuilder(line.strings).directory(new File(line.cd)).start();

                final InputStream is = process.getInputStream();
                final OutputStream os = process.getOutputStream();
                final InputStream es = process.getErrorStream();

                final long startedAt = System.currentTimeMillis();

                final boolean[] processFinished = {false};

                final Process finalProcess = process;

                GlobalContext.INSTANCE.localExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        long now = -1;
                        while (true) {
                            now = System.currentTimeMillis();
                            try {
                                Thread.sleep(50);

                                int lengthBefore = sb.length();

                                while (is.available() > 0) {
                                    sb.append((char) is.read());
                                }

                                if (sb.length() != lengthBefore) {
                                    System.out.print(sb.subSequence(lengthBefore, sb.length()));
                                }

                                if (processFinished[0] || now - startedAt > processTimeoutMs) {
                                    break;
                                }
                            } catch (Exception e) {
                                logger.info("", e);
                                break;
                            }
                        }

                        if (now - startedAt > processTimeoutMs) {
                            finalProcess.destroy();
                        }
                    }
                });

                final int exitCode = process.waitFor();

                return new ProcessResult(exitCode, sb.toString());
            } catch (Exception e) {
                logger.info("", e);
                return new ProcessResult(-1, sb.toString());
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }
    }

    @Override
    public <T extends SvnScm.CommandLineResult> T run(BaseScm.CommandLine<T> line, final GenericUnixRemoteEnvironment.SshSession.WithSession inputCallback) {
        logger.debug("command: {}", line);

        final ProcessRunner.ProcessResult r = new ProcessRunner<T>(line).run();

        if (r.exitCode == -1) {
            return (T) new BaseScm.CommandLineResult(null, Result.ERROR);
        }

        return line.parseResult(r.text);
    }

    @Override
    public <T extends SvnScm.CommandLineResult> T runVCS(BaseScm.CommandLine<T> stringResultCommandLine) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.runVCS");
    }

    @Override
    public Result sftp(String dest, String host, String path, String user, String pw) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.sftp");
    }

    @Override
    public Result scpLocal(String dest, File... files) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.scpLocal");
    }

    @Override
    public Result mkdirs(String... dirs) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.mkdirs");
    }

    @Override
    protected Result copyOperation(String src, String dest, CopyCommandType type, boolean folder, String owner) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.copyOperation");
    }

    @Override
    public Result chown(String user, boolean recursive, String... dest) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.chown");
    }

    @Override
    public Result chmod(String octal, boolean recursive, String... files) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.chmod");
    }

    @Override
    public Result writeString(String path, String s) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.writeString");
    }

    @Override
    public String readString(String path, String _default) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.readString");
    }

    @Override
    public boolean exists(String path) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.exists");
    }

    @Override
    public String readLink(String path) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.readLink");
    }

    @Override
    public Result rm(String... paths) {
        throw new UnsupportedOperationException("todo GenericUnixLocalEnvironment.rm");
    }
}