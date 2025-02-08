import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.ContentType;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

import static javax.sound.sampled.FloatControl.Type.SAMPLE_RATE;

public class Main {

    private static final int SAMPLE_RATE = 22050; // 采样率

    private static final int BITS_PER_SAMPLE = 16; // 采样位数

    private static final int CHANNELS = 1; // 单声道

    public static void main(String[] args) throws IOException {
        Map paramMap = new HashMap();
        paramMap.put("tts_text","春节的凌晨，窗外传来一阵呼啸声，把我从睡梦中惊醒。我朦胧地睁开眼睛，窗外已是一片白色， 哦！看来没睡醒 ，我躺了下去。 哇！下雪了诶！太好喽！ 外面传来孩子们的惊呼声，我...");
        paramMap.put("spk_id","中文女");
        paramMap.put("prompt_text","希望你以后能够做的比我还好呦。");
        paramMap.put("instruct_text","Theo \\'Crimson\\', is a fiery, passionate rebel leader. \\\n" +
                "                                 Fights with fervor for justice, but struggles with impulsiveness.");
        try {
            byte[] response = HttpClientUtil.doPostByte("http://10.0.28.24:50000/inference_zero_shot", paramMap, "zero_shot_prompt.wav");
            if (response != null && response.length > 0) {
                // 将字节数组转换为int16数组
                short[] audioData = new short[response.length / 2];
                ByteBuffer.wrap(response).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioData);

                // 创建WAV文件格式
                AudioFormat format = new AudioFormat(
                        SAMPLE_RATE,          // 采样率
                        BITS_PER_SAMPLE,      // 采样位数
                        CHANNELS,             // 声道数
                        true,                 // 是否有符号（signed）
                        false                 // 字节序（false=小端序）
                );

                // 创建音频流
                byte[] wavData = new byte[audioData.length * 2];
                ByteBuffer.wrap(wavData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(audioData);

                AudioInputStream audioInputStream = new AudioInputStream(
                        new ByteArrayInputStream(wavData),
                        format,
                        audioData.length
                );

                // 保存为WAV文件
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File("output.wav"));
                System.out.println("音频文件已保存到 output.wav");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
