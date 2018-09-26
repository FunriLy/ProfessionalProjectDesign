package com.xiaomi.infra.ec;

/**
 * Time: Created by FunriLy on 2018/9/26.
 * Motto: From small beginnings comes great things.
 * Description:
 *          CodecInterface defines the interfaces the a codec class must implement.
 * @author FunriLy
 */
public interface CodecInterface {

    /**
     * Encodes specified data blocks. This method is thread safe and reenterable.
     *
     * @param data The data blocks matrix
     * @return The coding blocks matrix
     */
    public byte[][] encode(byte[][] data);

    /**
     * Decodes specified failed data blocks. This method is thread safe and
     * reenterable.
     *
     * @param erasures The failed data blocks list
     * @param data The data blocks matrix
     * @param coding The coding blocks matrix
     */
    public void decode(int[] erasures, byte[][]data, byte[][] coding);
}