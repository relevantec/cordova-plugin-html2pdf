/*
 * The MIT License
 *
 * Copyright 2016 BCL Technologies.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.easypdfcloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.lukhnos.nnio.file.Path;
import org.lukhnos.nnio.file.Paths;
import java.util.logging.Level;

/**
 *
 * @author BCL Technologies
 */
public class LocalFileTokenManager implements OAuth2TokenManager {
    private static final Object fileLock = new Object();

    private TokenInfo tokenInfo;
    private String filePath;

    private void Initialize(Path tokenFilePath) {
        this.tokenInfo = null;
        this.filePath = tokenFilePath.toString();

        Path parentPath = tokenFilePath.getParent();
        File fileDir = new File(parentPath.toString());
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
    }

    public LocalFileTokenManager(String clientId) {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path tokenFilePath = Paths.get(tempDir, "easyPdfCloud", "clients", clientId, "token.object");
        
        Initialize(tokenFilePath);
    }
    
    @Override
    public TokenInfo loadTokenInfo() throws IOException {
        if (this.tokenInfo == null) {
            File tokenFile = new File(this.filePath);
            if (tokenFile.exists()) {
                synchronized(LocalFileTokenManager.fileLock) {
                    if (this.tokenInfo == null) {
                        FileInputStream fis = new FileInputStream(this.filePath);
                        try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                            Object object = ois.readObject();
                            this.tokenInfo = (TokenInfo)object;
                        } catch (ClassNotFoundException e) {
                            LogUtils.log(LocalFileTokenManager.class.getName(), Level.INFO, null, e);
                            // The file is in incompatible format.  Delete it
                            tokenFile.delete();
                        }
                    }
                }
            }
        }

        return this.tokenInfo;
    }

    @Override
    public void saveTokenInfo(TokenInfo tokenInfo) throws IOException {
        this.tokenInfo = tokenInfo;
        
        synchronized(LocalFileTokenManager.fileLock) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.filePath))) {
                oos.writeObject(tokenInfo);
            }
        }
    }
}
