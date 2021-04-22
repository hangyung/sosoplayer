/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <jni.h>
#include <stdlib.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <pthread.h>

extern "C" {
#ifdef __cplusplus
#define __STDC_CONSTANT_MACROS
#ifdef _STDINT_H
#undef _STDINT_H
#endif
#include <stdint.h>
#endif
#include <libavutil/samplefmt.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavformat/avio_internal.h>
#include <libavformat/internal.h>
#include <libavutil/channel_layout.h>
#include <libavutil/error.h>
#include <libavutil/opt.h>
#include <libswresample/swresample.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
}
#define OUTPUT_BUFFER_SIZE (1048576)
#define FFMPEG_DECODE_ONLY (1000)
#define TIME_UNSET (-9223372036854775807l)

#define LOG_TAG "ffmpeg_jni"
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, \
                   __VA_ARGS__))

#define LIBRARY_FUNC(RETURN_TYPE, NAME, ...)                              \
  extern "C" {                                                            \
  JNIEXPORT RETURN_TYPE                                                   \
      Java_com_google_android_exoplayer2_ext_ffmpeg2_FfmpegLibrary_##NAME( \
          JNIEnv *env, jobject thiz, ##__VA_ARGS__);                      \
  }                                                                       \
  JNIEXPORT RETURN_TYPE                                                   \
      Java_com_google_android_exoplayer2_ext_ffmpeg2_FfmpegLibrary_##NAME( \
          JNIEnv *env, jobject thiz, ##__VA_ARGS__)

#define AUDIO_DECODER_FUNC(RETURN_TYPE, NAME, ...)                             \
  extern "C" {                                                                 \
  JNIEXPORT RETURN_TYPE                                                        \
      Java_com_google_android_exoplayer2_ext_ffmpeg2_FfmpegAudioDecoder_##NAME( \
          JNIEnv *env, jobject thiz, ##__VA_ARGS__);                           \
  }                                                                            \
  JNIEXPORT RETURN_TYPE                                                        \
      Java_com_google_android_exoplayer2_ext_ffmpeg2_FfmpegAudioDecoder_##NAME( \
          JNIEnv *env, jobject thiz, ##__VA_ARGS__)


#define FFMPEG_DEMUXER_FUNC(RETURN_TYPE, NAME, ...)                             \
  extern "C" {                                                                 \
  JNIEXPORT RETURN_TYPE                                                        \
      Java_com_google_android_exoplayer2_ext_ffmpeg2_FfmpegDemuxer_##NAME( \
          JNIEnv *env, jobject thiz, ##__VA_ARGS__);                           \
  }                                                                            \
  JNIEXPORT RETURN_TYPE                                                        \
      Java_com_google_android_exoplayer2_ext_ffmpeg2_FfmpegDemuxer_##NAME( \
          JNIEnv *env, jobject thiz, ##__VA_ARGS__)

#define VIDEO_DECODER_FUNC(RETURN_TYPE, NAME, ...)                       \
  extern "C" {                                                                 \
  JNIEXPORT RETURN_TYPE                                                        \
      Java_com_google_android_exoplayer2_ext_ffmpeg2_FfmpegVideoDecoder_##NAME( \
          JNIEnv *env, jobject thiz, ##__VA_ARGS__);                           \
  }                                                                            \
  JNIEXPORT RETURN_TYPE                                                        \
      Java_com_google_android_exoplayer2_ext_ffmpeg2_FfmpegVideoDecoder_##NAME( \
          JNIEnv *env, jobject thiz, ##__VA_ARGS__)


#define ERROR_STRING_BUFFER_LENGTH 256
#define MAX_FRAMES (32)
#define KEY_DECODING_PRIVATE_BASE  (0x1000)

#define RESIZE_WIDTH  (320)

// Output format corresponding to AudioFormat.ENCODING_PCM_16BIT.
static const AVSampleFormat OUTPUT_FORMAT_PCM_16BIT = AV_SAMPLE_FMT_S16;
// Output format corresponding to AudioFormat.ENCODING_PCM_FLOAT.
static const AVSampleFormat OUTPUT_FORMAT_PCM_FLOAT = AV_SAMPLE_FMT_FLT;

// LINT.IfChange
static const int AUDIO_DECODER_ERROR_INVALID_DATA = -1;
static const int AUDIO_DECODER_ERROR_OTHER = -2;
// LINT.ThenChange(../java/com/google/android/exoplayer2/ext/ffmpeg2/FfmpegAudioDecoder.java)

/**
 * Returns the AVCodec with the specified name, or NULL if it is not available.
 */
AVCodec *getCodecByName(JNIEnv *env, jstring codecName);

/**
 * Allocates and opens a new AVCodecContext for the specified codec, passing the
 * provided extraData as initialization data for the decoder if it is non-NULL.
 * Returns the created context.
 */
AVCodecContext *createContext(JNIEnv *env, AVCodec *codec, jbyteArray extraData,
                              jboolean outputFloat, jint rawSampleRate,
                              jint rawChannelCount);


/**
 * Decodes the packet into the output buffer, returning the number of bytes
 * written, or a negative AUDIO_DECODER_ERROR constant value in the case of an
 * error.
 */
int decodePacket(AVCodecContext *context, AVPacket *packet,
                 uint8_t *outputBuffer, int outputSize);

/**
 * Outputs a log message describing the avcodec error number.
 */
void logError(const char *functionName, int errorNumber);

/**
 * Releases the specified context.
 */
void releaseContext(AVCodecContext *context);


int open_codec_context(int *stream_idx,
                       AVCodecContext **dec_ctx, AVFormatContext *fmt_ctx, enum AVMediaType type);

static JavaVM *jvm;
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env;
  if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
    return -1;
  }
  jvm = vm;
  av_register_all();
  avcodec_register_all();
  return JNI_VERSION_1_6;
}

LIBRARY_FUNC(jstring, ffmpegGetVersion) {
  return env->NewStringUTF(LIBAVCODEC_IDENT);
}

LIBRARY_FUNC(jint, ffmpegGetInputBufferPaddingSize) {
  return (jint) AV_INPUT_BUFFER_PADDING_SIZE;
}

LIBRARY_FUNC(jboolean, ffmpegHasDecoder, jstring codecName) {
  return getCodecByName(env, codecName) != NULL;
}

AUDIO_DECODER_FUNC(jlong, ffmpegInitialize, jstring codecName,
                   jbyteArray extraData, jboolean outputFloat,
                   jint rawSampleRate, jint rawChannelCount) {
  AVCodec *codec = getCodecByName(env, codecName);
  if (!codec) {
    LOGE("Codec not found.");
    return 0L;
  }
  return (jlong) createContext(env, codec, extraData, outputFloat, rawSampleRate,
                               rawChannelCount);
}

AUDIO_DECODER_FUNC(jint, ffmpegDecode, jlong context, jobject inputData,
                   jint inputSize, jobject outputData, jint outputSize) {
  if (!context) {
    LOGE("Context must be non-NULL.");
    return -1;
  }
  if (!inputData || !outputData) {
    LOGE("Input and output buffers must be non-NULL.");
    return -1;
  }
  if (inputSize < 0) {
    LOGE("Invalid input buffer size: %d.", inputSize);
    return -1;
  }
  if (outputSize < 0) {
    LOGE("Invalid output buffer length: %d", outputSize);
    return -1;
  }
  uint8_t *inputBuffer = (uint8_t *) env->GetDirectBufferAddress(inputData);
  uint8_t *outputBuffer = (uint8_t *) env->GetDirectBufferAddress(outputData);
  AVPacket packet;
  av_init_packet(&packet);
  packet.data = inputBuffer;
  packet.size = inputSize;
  return decodePacket((AVCodecContext *) context, &packet, outputBuffer,
                      outputSize);
}

AUDIO_DECODER_FUNC(jint, ffmpegGetChannelCount, jlong context) {
  if (!context) {
    LOGE("Context must be non-NULL.");
    return -1;
  }
  return ((AVCodecContext *) context)->channels;
}

AUDIO_DECODER_FUNC(jint, ffmpegGetSampleRate, jlong context) {
  if (!context) {
    LOGE("Context must be non-NULL.");
    return -1;
  }
  return ((AVCodecContext *) context)->sample_rate;
}

AUDIO_DECODER_FUNC(jlong, ffmpegReset, jlong jContext, jbyteArray extraData) {
  AVCodecContext *context = (AVCodecContext *) jContext;
  if (!context) {
    LOGE("Tried to reset without a context.");
    return 0L;
  }

  AVCodecID codecId = context->codec_id;
  if (codecId == AV_CODEC_ID_TRUEHD) {
    // Release and recreate the context if the codec is TrueHD.
    // TODO: Figure out why flushing doesn't work for this codec.
    releaseContext(context);
    AVCodec *codec = avcodec_find_decoder(codecId);
    if (!codec) {
      LOGE("Unexpected error finding codec %d.", codecId);
      return 0L;
    }
    jboolean outputFloat =
        (jboolean) (context->request_sample_fmt == OUTPUT_FORMAT_PCM_FLOAT);
    return (jlong) createContext(env, codec, extraData, outputFloat,
        /* rawSampleRate= */ -1,
        /* rawChannelCount= */ -1);
  }

  avcodec_flush_buffers(context);
  return (jlong) context;
}

AUDIO_DECODER_FUNC(void, ffmpegRelease, jlong context) {
  if (context) {
    releaseContext((AVCodecContext *) context);
  }
}


struct JniFrameBuffer {
  friend class JniBufferManager;

  //AVFrame* avFrame;
  uint8_t *pointers[4];
  int linesizes[4];
  int width;
  int height;

 private:
  int id;
  int ref_count;
};

class JniBufferManager {
  JniFrameBuffer *all_buffers[MAX_FRAMES];
  int all_buffer_count = 0;

  JniFrameBuffer *free_buffers[MAX_FRAMES];
  int free_buffer_count = 0;

  pthread_mutex_t mutex;

 public:
  JniBufferManager() { pthread_mutex_init(&mutex, NULL); }

  ~JniBufferManager() {
    while (all_buffer_count--) {
      if (all_buffers[all_buffer_count]->pointers[0] != NULL) {
        av_freep(&all_buffers[all_buffer_count]->pointers[0]);
        all_buffers[all_buffer_count]->pointers[0] = NULL;
      }
    }
  }

  int set_buffer(uint8_t *pointers[4], int linesizes[4], int w, int h) {
    pthread_mutex_lock(&mutex);
    JniFrameBuffer *out_buffer;
    int ret = 0;
    if (free_buffer_count) {
      out_buffer = free_buffers[--free_buffer_count];
      if (out_buffer->width != w || out_buffer->height != h) {
        av_freep(&out_buffer->pointers[0]);
        ret = av_image_alloc(out_buffer->pointers,
                             out_buffer->linesizes,
                             w,
                             h,
                             AV_PIX_FMT_YUV420P,
                             16);
        out_buffer->width = w;
        out_buffer->height = h;
      }
    } else {
      out_buffer = new JniFrameBuffer();
      out_buffer->id = all_buffer_count;
      all_buffers[all_buffer_count++] = out_buffer;
      out_buffer->width = w;
      out_buffer->height = h;
      //out_buffer->avFrame = av_frame_alloc();
      ret =
          av_image_alloc(out_buffer->pointers, out_buffer->linesizes, w, h, AV_PIX_FMT_YUV420P, 16);
    }

    int retVal = out_buffer->id;
    if (ret < 0 || all_buffer_count >= MAX_FRAMES) {
      LOGE("XXX JniBufferManager get_buffer OOM. all_buffer_count %d", all_buffer_count);
      retVal = -1;
    }

    av_image_copy(out_buffer->pointers,
                  out_buffer->linesizes,
                  (const uint8_t **) pointers,
                  linesizes,
                  AV_PIX_FMT_YUV420P,
                  w,
                  h);
    out_buffer->ref_count = 1;
    pthread_mutex_unlock(&mutex);
    return retVal;
  }

  JniFrameBuffer *get_buffer(int id) const {
    if (id < 0 || id >= all_buffer_count) {
      LOGE("JniBufferManager get_buffer invalid id %d.", id);
      return NULL;
    }
    return all_buffers[id];
  }

  int release(int id) {
    if (id < 0 || id >= all_buffer_count) {
      LOGE("JniBufferManager release invalid id %d.", id);
      return -1;
    }
    pthread_mutex_lock(&mutex);
    JniFrameBuffer *buffer = all_buffers[id];
    if (!buffer->ref_count) {
      LOGE("JniBufferManager release, buffer already released.");
      pthread_mutex_unlock(&mutex);
      return -1;
    }
    if (!--buffer->ref_count) {
      free_buffers[free_buffer_count++] = buffer;
    }
    pthread_mutex_unlock(&mutex);
    return 0;
  }
};

struct FfmpegCodec {
  int stream_type;
  AVCodecContext *dec_ctx;    // libavcodec.a
  int stream_idx;
  bool needDecodingAudio;

  struct SwsContext *img_convert_ctx;
  AVFrame *convertFrame;
};


struct FFMKVDecoderImpl {
  AVFormatContext *fmt_ctx;    // libavformat.a

  FfmpegCodec **codecs;
  int video_stream_idx;
  int audio_stream_idx;

  AVPacket pkt;

  jclass ffmpegPacketClass;
  jmethodID ffmepgPacketSetMethod;


  jclass ffmpegCustomIoClass;
  jfieldID ffmpegCustomIoByteBuffer;
  jmethodID ffmepgCustomIoReadMethod;
  jmethodID ffmepgCustomIoSeekMethod;
  jobject ffCustomIoObject;
  uint8_t *tempBuffer;

  uint8_t *pcmBuffer;
  int pcmBufferSize;
  JniBufferManager *bufferManager;

  jmethodID initForYuvFrame;
  jmethodID initForPrivateFrame;

  jfieldID dataField;
  jfieldID decoderPrivateField;

  ANativeWindow *native_window;
  jobject surface;
  int width;
  int height;

};


static int readForCustomIo(void *ptr, uint8_t *buffer, int bufferLength) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) ptr;
  if (pImpl == NULL) {
    return -1;
  }
  if (bufferLength == 0)
    return 0;

  int size = bufferLength > OUTPUT_BUFFER_SIZE ? OUTPUT_BUFFER_SIZE : bufferLength;
  JNIEnv *env;
  jvm->AttachCurrentThread(&env, NULL);


  long readSize =
      env->CallLongMethod(pImpl->ffCustomIoObject, pImpl->ffmepgCustomIoReadMethod, size);
  if (readSize > 0) {
    jobject
        byteBuffer = env->GetObjectField(pImpl->ffCustomIoObject, pImpl->ffmpegCustomIoByteBuffer);
    uint8_t *pt = (uint8_t *) env->GetDirectBufferAddress(byteBuffer);
    memcpy(buffer, pt, (int) readSize);
  }
  return readSize;
}

static int64_t seekForCustomIo(void *ptr, int64_t pos, int whence) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) ptr;
  if (pImpl == NULL) {
    return -1;
  }
  JNIEnv *env;
  jvm->AttachCurrentThread(&env, NULL);

  jlong ret = env->CallLongMethod(pImpl->ffCustomIoObject,
                                  pImpl->ffmepgCustomIoSeekMethod,
                                  (jlong) pos,
                                  (jint) whence);

  return ret;
}


FFMKVDecoderImpl *createFFMKVDecoderImpl(JNIEnv *env, jobject customIo) {
  FFMKVDecoderImpl *pImpl = new FFMKVDecoderImpl;
  memset(pImpl, 0, sizeof(FFMKVDecoderImpl));
  pImpl->bufferManager = new JniBufferManager();
  pImpl->ffmpegPacketClass =
      env->FindClass("com/google/android/exoplayer2/ext/ffmpeg2/FfmpegPacket");
  pImpl->ffmepgPacketSetMethod =
      env->GetMethodID(pImpl->ffmpegPacketClass, "set", "(ILjava/nio/ByteBuffer;JJI)V");


  if (customIo != NULL) {
    pImpl->ffCustomIoObject = env->NewGlobalRef(customIo);
    pImpl->ffmpegCustomIoClass = env->GetObjectClass(pImpl->ffCustomIoObject);
    pImpl->ffmpegCustomIoByteBuffer =
        env->GetFieldID(pImpl->ffmpegCustomIoClass, "byteBuffer", "Ljava/nio/ByteBuffer;");
    pImpl->ffmepgCustomIoReadMethod = env->GetMethodID(pImpl->ffmpegCustomIoClass, "read", "(I)J");
    pImpl->ffmepgCustomIoSeekMethod = env->GetMethodID(pImpl->ffmpegCustomIoClass, "seek", "(JI)J");
    pImpl->tempBuffer = (uint8_t *) av_malloc(OUTPUT_BUFFER_SIZE);
    memset(pImpl->tempBuffer, 0, OUTPUT_BUFFER_SIZE);
  }

  pImpl->video_stream_idx = -1;
  pImpl->audio_stream_idx = -1;

  const jclass outputBufferClass = env->FindClass(
      "com/google/android/exoplayer2/video/VideoDecoderOutputBuffer");
  pImpl->initForYuvFrame = env->GetMethodID(outputBufferClass, "initForYuvFrame",
                                            "(IIIII)Z");
  pImpl->initForPrivateFrame =
      env->GetMethodID(outputBufferClass, "initForPrivateFrame", "(II)V");
  pImpl->dataField = env->GetFieldID(outputBufferClass, "data",
                                     "Ljava/nio/ByteBuffer;");

  pImpl->decoderPrivateField =
      env->GetFieldID(outputBufferClass, "decoderPrivate", "I");

  pImpl->native_window = NULL;
  pImpl->surface = NULL;
//  pImpl->needDecodingAudio = false;

  pImpl->pcmBuffer = (uint8_t *) av_malloc(OUTPUT_BUFFER_SIZE);
  pImpl->pcmBufferSize = OUTPUT_BUFFER_SIZE;
  memset(pImpl->pcmBuffer, 0, pImpl->pcmBufferSize);
  av_init_packet(&pImpl->pkt);
  return pImpl;
}


FFMPEG_DEMUXER_FUNC(long, FfmpegDemuxerOpen, jstring path) {

  FFMKVDecoderImpl *pImpl = createFFMKVDecoderImpl(env, NULL);

  const char *pathChars = env->GetStringUTFChars(path, NULL);
  if (avformat_open_input(&pImpl->fmt_ctx, pathChars, NULL, NULL) < 0) {
    LOGE ("Could not open source file %s", pathChars);
    env->ReleaseStringUTFChars(path, pathChars);
    free(pImpl);
    return 0;
  }
  env->ReleaseStringUTFChars(path, pathChars);

  if (avformat_find_stream_info(pImpl->fmt_ctx, NULL) < 0) {
    LOGE ("Could not find stream information");
    avformat_close_input(&pImpl->fmt_ctx);
    free(pImpl);
    return 0;
  }

  pImpl->codecs = (FfmpegCodec **) av_malloc(pImpl->fmt_ctx->nb_streams * sizeof(FfmpegCodec));
  memset(pImpl->codecs, 0, pImpl->fmt_ctx->nb_streams * sizeof(FfmpegCodec));
  for (int i = 0; i < pImpl->fmt_ctx->nb_streams; i++) {
    pImpl->codecs[i] = (FfmpegCodec *) av_malloc(sizeof(FfmpegCodec));
    memset(pImpl->codecs[i], 0, sizeof(FfmpegCodec));
  }
  for (int i = 0; i < pImpl->fmt_ctx->nb_streams; i++) {
    pImpl->codecs[i]->stream_type = -1;
  }

  return (jlong) pImpl;
}


FFMPEG_DEMUXER_FUNC(jlong, FfmpegDemuxerCustomOpen, jobject customIo) {
  FFMKVDecoderImpl *pImpl = createFFMKVDecoderImpl(env, customIo);

  long readLength = readForCustomIo(pImpl, pImpl->tempBuffer, OUTPUT_BUFFER_SIZE);
  seekForCustomIo(pImpl, 0, SEEK_SET);

  AVProbeData probeData;
  probeData.buf = pImpl->tempBuffer; // Buffer must have AVPROBE_PADDING_SIZE.
  probeData.buf_size = readLength;
  probeData.filename = "";

  pImpl->fmt_ctx = avformat_alloc_context();

  AVIOContext *customIoCtx = avio_alloc_context(pImpl->tempBuffer,
                                                readLength,
                                                0,
                                                pImpl,
                                                readForCustomIo,
                                                NULL,
                                                seekForCustomIo);
  pImpl->fmt_ctx->pb = customIoCtx;

  pImpl->fmt_ctx->iformat = av_probe_input_format(&probeData, 1);
  pImpl->fmt_ctx->flags = AVFMT_FLAG_CUSTOM_IO;

  if (avformat_open_input(&pImpl->fmt_ctx, "", NULL, NULL) < 0) {
    LOGE ("Could not open custom source file");
    if (pImpl->fmt_ctx != NULL) {
      avformat_free_context(pImpl->fmt_ctx);
    }
    free(pImpl);
    return 0;
  }


  if (avformat_find_stream_info(pImpl->fmt_ctx, NULL) < 0) {
    LOGE ("Could not find stream information");
    avformat_close_input(&pImpl->fmt_ctx);
    free(pImpl);
    return 0;
  }

  pImpl->codecs = (FfmpegCodec **) av_malloc(pImpl->fmt_ctx->nb_streams * sizeof(FfmpegCodec));
  memset(pImpl->codecs, 0, pImpl->fmt_ctx->nb_streams * sizeof(FfmpegCodec));
  for (int i = 0; i < pImpl->fmt_ctx->nb_streams; i++) {
    pImpl->codecs[i] = (FfmpegCodec *) av_malloc(sizeof(FfmpegCodec));
    memset(pImpl->codecs[i], 0, sizeof(FfmpegCodec));
  }
  for (int i = 0; i < pImpl->fmt_ctx->nb_streams; i++) {
    pImpl->codecs[i]->stream_type = -1;
  }

  return (jlong) pImpl;
}


FFMPEG_DEMUXER_FUNC(jint, FfmpegDemuxerGetStreamCount, jlong context) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return 0;
  return pImpl->fmt_ctx->nb_streams;
}

FFMPEG_DEMUXER_FUNC(jint, FfmpegDemuxerGetStreamType, jlong context, jint index) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return 0;
  return pImpl->fmt_ctx->streams[index]->codec->codec_type;
}


FFMPEG_DEMUXER_FUNC(jint, FfmpegDemuxerOpenStream, jlong context, jint index) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return -1;
  int ret = 0;
  AVDictionary *opts = NULL;
  AVStream *st = pImpl->fmt_ctx->streams[index];
  /* find decoder for the stream */
  AVCodec *codec = avcodec_find_decoder(st->codecpar->codec_id);
  if (!codec) {
    LOGE("Failed to find %s codec\n",
         av_get_media_type_string(pImpl->fmt_ctx->streams[index]->codec->codec_type));
    return AVERROR(EINVAL);
  }

  /* Allocate a codec context for the decoder */
  AVCodecContext *codecContext = avcodec_alloc_context3(codec);
  if (!codecContext) {
    LOGE("Failed to allocate the %s codec context\n",
         av_get_media_type_string(pImpl->fmt_ctx->streams[index]->codec->codec_type));
    return AVERROR(ENOMEM);
  }

  /* Copy codec parameters from input stream to output codec context */
  if ((ret = avcodec_parameters_to_context(codecContext, st->codecpar)) < 0) {
    LOGE("Failed to copy %s codec parameters to decoder context\n",
         av_get_media_type_string(pImpl->fmt_ctx->streams[index]->codec->codec_type));
    return ret;
  }

  /* Init the decoders */
  if ((ret = avcodec_open2(codecContext, codec, &opts)) < 0) {
    LOGE("Failed to open %s codec\n",
         av_get_media_type_string(pImpl->fmt_ctx->streams[index]->codec->codec_type));
    return ret;
  }
  pImpl->codecs[index]->dec_ctx = codecContext;
  pImpl->codecs[index]->stream_idx = index;
  pImpl->codecs[index]->stream_type = pImpl->fmt_ctx->streams[index]->codec->codec_type;
  pImpl->codecs[index]->img_convert_ctx = NULL;


  if (pImpl->fmt_ctx->streams[index]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
    if (pImpl->video_stream_idx < 0) {
      pImpl->video_stream_idx = index; // use only seek index
    }

    AVCodecContext *avCodecContext = pImpl->fmt_ctx->streams[index]->codec;
    bool formatMiss = avCodecContext->pix_fmt != AV_PIX_FMT_YUV420P;
    bool sizeMiss =
        avCodecContext->bits_per_raw_sample != 0 && avCodecContext->bits_per_raw_sample != 8;
    if (formatMiss || sizeMiss) {
      int resizeHeight = avCodecContext->height * RESIZE_WIDTH / avCodecContext->width;
      resizeHeight = (resizeHeight + 1) / 2 * 2;
      pImpl->codecs[index]->img_convert_ctx =
          sws_getCachedContext(pImpl->codecs[index]->img_convert_ctx,
                               avCodecContext->width, avCodecContext->height,
                               avCodecContext->pix_fmt, RESIZE_WIDTH, resizeHeight,
                               AV_PIX_FMT_YUV420P, SWS_BICUBIC, NULL, NULL, NULL);

      pImpl->codecs[index]->convertFrame = av_frame_alloc();
      av_image_alloc(pImpl->codecs[index]->convertFrame->data,
                     pImpl->codecs[index]->convertFrame->linesize,
                     RESIZE_WIDTH,
                     resizeHeight,
                     AV_PIX_FMT_YUV420P,
                     16);
      pImpl->codecs[index]->convertFrame->width = RESIZE_WIDTH;
      pImpl->codecs[index]->convertFrame->height = resizeHeight;
    } else {
      pImpl->codecs[index]->img_convert_ctx = NULL;
    }

  } else if (pImpl->fmt_ctx->streams[index]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
    if (pImpl->audio_stream_idx < 0) {
      pImpl->audio_stream_idx = index; // use only seek index
    }

    switch (codecContext->codec_id) {
      case AV_CODEC_ID_AAC:
        break;
      default:
        pImpl->codecs[index]->needDecodingAudio = true;
        codecContext->request_sample_fmt = OUTPUT_FORMAT_PCM_16BIT;
        break;
    }
  }

  return ret;

}


FFMPEG_DEMUXER_FUNC(jlong, FfmpegDemuxerGetDuration, jlong context) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return -1;
  return pImpl->fmt_ctx->duration;
}

FFMPEG_DEMUXER_FUNC(jstring, FfmpegDemuxerGetCodecId, jlong context, jint index) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return env->NewStringUTF("Unknown");
  return env->NewStringUTF(avcodec_get_name(pImpl->codecs[index]->dec_ctx->codec_id));
}

FFMPEG_DEMUXER_FUNC(jint, FfmpegDemuxerGetAudioChannelCount, jlong context, jint index) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return 0;
  return av_get_channel_layout_nb_channels(pImpl->codecs[index]->dec_ctx->channel_layout);
}

FFMPEG_DEMUXER_FUNC(jint, FfmpegDemuxerGetAudioSampleRate, jlong context, jint index) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return 0;
  return pImpl->codecs[index]->dec_ctx->sample_rate;
}

FFMPEG_DEMUXER_FUNC(jint, FfmpegDemuxerGetVideoWidth, jlong context, jint index) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return 0;
  return pImpl->codecs[index]->dec_ctx->width;
}

FFMPEG_DEMUXER_FUNC(jint, FfmpegDemuxerGetVideoHeight, jlong context, jint index) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return 0;
  return pImpl->codecs[index]->dec_ctx->height;
}

static size_t GetSizeWidth(size_t x) {
  size_t n = 1;
  while (x > 127) {
    ++n;
    x >>= 7;
  }
  return n;
}

static uint8_t *EncodeSize(uint8_t *dst, size_t x) {
  while (x > 127) {
    *dst++ = (x & 0x7f) | 0x80;
    x >>= 7;
  }
  *dst++ = x;
  return dst;
}

FFMPEG_DEMUXER_FUNC(jbyteArray, FfmpegDemuxerGetExtraData, jlong context, jint index) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return env->NewByteArray(0);
  int count = 0;
  if (pImpl->codecs[index]->dec_ctx->codec_id == AV_CODEC_ID_MPEG4
  && pImpl->codecs[index]->dec_ctx->extradata_size <= 0) {
    jbyteArray ret = NULL;
    while(true) {
      int result = av_read_frame(pImpl->fmt_ctx, &pImpl->pkt);
      if (result < 0) {
        avformat_seek_file(pImpl->fmt_ctx,
                                       index, INT64_MIN, 0, INT64_MAX, 0);
        return env->NewByteArray(0);
      }
      size_t i = 0;
      bool found = false;
      while (i + 3 < pImpl->pkt.size) {
        if (!memcmp("\x00\x00\x01\xb6",
                    pImpl->pkt.data + i, 4)) {
          found = true;
          break;
        }
        ++i;
      }
      if (!found) {
        av_free_packet(&pImpl->pkt);
        if (count++ < 100) {
          continue;
        } else {
          avformat_seek_file(pImpl->fmt_ctx,
                             index, INT64_MIN, 0, INT64_MAX, 0);
          return env->NewByteArray(0);
        }
      }

      ret = env->NewByteArray(i);
      env->SetByteArrayRegion(ret,
                              0,
                              i,
                              (jbyte *)pImpl->pkt.data);
      av_free_packet(&pImpl->pkt);
      break;

    }
    avformat_seek_file(pImpl->fmt_ctx,
                                   index, INT64_MIN, 0, INT64_MAX, 0);
    if (ret == NULL) {
      return env->NewByteArray(0);
    }
    return ret;
  }


  jbyteArray ret = env->NewByteArray(pImpl->codecs[index]->dec_ctx->extradata_size);
  env->SetByteArrayRegion(ret,
                          0,
                          pImpl->codecs[index]->dec_ctx->extradata_size,
                          (jbyte *) pImpl->codecs[index]->dec_ctx->extradata);
  return ret;
}
FfmpegCodec *getFfmpegCodec(FFMKVDecoderImpl *pImpl, int stream_index) {
  return pImpl->codecs[stream_index];
}

FFMPEG_DEMUXER_FUNC(jint, FfmpegDemuxerReadPacket, jlong context, jobject ffmpegPacket) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return -1;
  int ret = av_read_frame(pImpl->fmt_ctx, &pImpl->pkt);
  if (ret < 0)
    return ret;
  AVPacket *pkt = &pImpl->pkt;

  jlong pts = TIME_UNSET;
  jlong dts = TIME_UNSET;
  FfmpegCodec *ffmpegCodec = getFfmpegCodec(pImpl, pkt->stream_index);
  switch (ffmpegCodec->stream_type) {
    case AVMEDIA_TYPE_VIDEO: {
//        jbyteArray data = env->NewByteArray(pkt->size);
//        env->SetByteArrayRegion (data, 0, pkt->size, (jbyte *)pkt->data);
      jobject data = env->NewDirectByteBuffer(pkt->data, pkt->size);

      if (pkt->pts != AV_NOPTS_VALUE) {
        pts = pkt->pts
            * (av_q2d(pImpl->fmt_ctx->streams[pkt->stream_index]->time_base) * AV_TIME_BASE);
      }

      if (pkt->dts != AV_NOPTS_VALUE) {
        dts = pkt->dts
            * (av_q2d(pImpl->fmt_ctx->streams[pkt->stream_index]->time_base) * AV_TIME_BASE);
      }

      env->CallVoidMethod(ffmpegPacket,
                          pImpl->ffmepgPacketSetMethod,
                          pkt->stream_index,
                          data,
                          pts,
                          dts,
                          pkt->flags);
      av_packet_unref(&pImpl->pkt);
    }
      break;
    case AVMEDIA_TYPE_AUDIO: {
      if (pkt->pts != AV_NOPTS_VALUE) {
        pts = pkt->pts
            * (av_q2d(pImpl->fmt_ctx->streams[pkt->stream_index]->time_base) * AV_TIME_BASE);
      }

      if (pkt->pts != AV_NOPTS_VALUE) {
        dts = pkt->dts
            * (av_q2d(pImpl->fmt_ctx->streams[pkt->stream_index]->time_base) * AV_TIME_BASE);
      }

      if (ffmpegCodec->needDecodingAudio) {
        int outSize =
            decodePacket(ffmpegCodec->dec_ctx, pkt, pImpl->pcmBuffer, pImpl->pcmBufferSize);
        if (outSize > 0) {
//            jbyteArray data = env->NewByteArray(outSize);
//            env->SetByteArrayRegion (data, 0, outSize, (jbyte *) pImpl->pcmBuffer);
          jobject data = env->NewDirectByteBuffer(pImpl->pcmBuffer, outSize);
          env->CallVoidMethod(ffmpegPacket,
                              pImpl->ffmepgPacketSetMethod,
                              pkt->stream_index,
                              data,
                              pts,
                              dts,
                              pkt->flags);
        } else {
          ret = outSize;
        }
      } else {
//          jbyteArray data = env->NewByteArray(pkt->size);
//          env->SetByteArrayRegion (data, 0, pkt->size, (jbyte *)pkt->data);
        jobject data = env->NewDirectByteBuffer(pkt->data, pkt->size);
        env->CallVoidMethod(ffmpegPacket,
                            pImpl->ffmepgPacketSetMethod,
                            pkt->stream_index,
                            data,
                            pts,
                            dts,
                            pkt->flags);
        av_packet_unref(&pImpl->pkt);
      }
    }
      break;
    case AVMEDIA_TYPE_UNKNOWN:
      ret = -1;
      break;
  }


  return ret;
}


FFMPEG_DEMUXER_FUNC(jint, FfmpegDemuxerSeekTo, jlong context, jlong seekTimeUs) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return -1;
  int64_t seek_target = seekTimeUs;

  if (pImpl->video_stream_idx >= 0) {
    seek_target = av_rescale_q(seek_target, AV_TIME_BASE_Q,
                               pImpl->fmt_ctx->streams[pImpl->video_stream_idx]->time_base);

    int error = avformat_seek_file(pImpl->fmt_ctx,
                                   pImpl->video_stream_idx, INT64_MIN, seek_target, INT64_MAX, 0);
    return error;
  } else if (pImpl->audio_stream_idx >= 0) {
    seek_target = av_rescale_q(seek_target, AV_TIME_BASE_Q,
                               pImpl->fmt_ctx->streams[pImpl->audio_stream_idx]->time_base);

    int error = avformat_seek_file(pImpl->fmt_ctx,
                                   pImpl->audio_stream_idx, INT64_MIN, seek_target, INT64_MAX, 0);
    return error;
  }
  return -1;
}

FFMPEG_DEMUXER_FUNC(long, FfmpegDemuxerClose, jlong context) {
  if (context) {
    FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;

    if (pImpl->fmt_ctx != NULL) {
      if (pImpl->codecs != NULL) {
        for (int i = 0; i < pImpl->fmt_ctx->nb_streams; i++) {
          if (pImpl->codecs[i] != NULL) {
            if (pImpl->codecs[i]->dec_ctx != NULL) {
              avcodec_free_context(&pImpl->codecs[i]->dec_ctx);
              av_free(pImpl->codecs[i]);
            }
            if (pImpl->codecs[i]->img_convert_ctx != NULL) {
              sws_freeContext(pImpl->codecs[i]->img_convert_ctx);
            }
            if (pImpl->codecs[i]->convertFrame != NULL) {
              av_freep(&pImpl->codecs[i]->convertFrame->data[0]);
              av_frame_free(&pImpl->codecs[i]->convertFrame);
            }
          }
        }
      }

      avformat_close_input(&pImpl->fmt_ctx);
    }

    if (pImpl->ffCustomIoObject != NULL) {
      env->DeleteGlobalRef(pImpl->ffCustomIoObject);
    }

    if (pImpl->pcmBuffer != NULL) {
      av_free(pImpl->pcmBuffer);
    }
    if (pImpl->tempBuffer != NULL) {
      av_free(pImpl->tempBuffer);
    }

    if (pImpl->bufferManager != NULL) {
      free(pImpl->bufferManager);
    }
    delete pImpl;
  }
}

VIDEO_DECODER_FUNC(jint,
                   FfmpegDecode,
                   jlong context,
                   jint streamIndex,
                   jobject encodedData,
                   jint length) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return -1;
  uint8_t *inputBuffer = (uint8_t *) env->GetDirectBufferAddress(encodedData);
  AVPacket packet;
  av_init_packet(&packet);
  packet.data = inputBuffer;
  packet.size = length;
  packet.stream_index = streamIndex;

  int result = 0;
  // Queue input data.
  result = avcodec_send_packet(pImpl->codecs[streamIndex]->dec_ctx, &packet);
  if (result) {
    logError("avcodec_send_packet", result);
    return result == AVERROR_INVALIDDATA ? AUDIO_DECODER_ERROR_INVALID_DATA
                                         : AUDIO_DECODER_ERROR_OTHER;
  }

  return result;
}


VIDEO_DECODER_FUNC(jint,
                   FfmpegGetFrame,
                   jlong context,
                   jint streamIndex,
                   jobject jOutputBuffer,
                   jint outputMode) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return -1;

  bool haveVideo = false;
  int ret = 0;
  while (ret >= 0) {
    AVFrame *avFrame = NULL;
    JniFrameBuffer *jfb = NULL;
    AVFrame *outFrame = NULL;
    avFrame = av_frame_alloc();
    ret = avcodec_receive_frame(pImpl->codecs[streamIndex]->dec_ctx, avFrame);
    if (ret < 0) {
      // those two return values are special and mean there is no output
      // frame available, but there were no errors during decoding
      av_frame_free(&avFrame);
      if (ret == AVERROR_EOF || ret == AVERROR(EAGAIN))
        return haveVideo ? 0 : FFMPEG_DECODE_ONLY;

      LOGE ("Error during decoding (%s)\n", av_err2str(ret));
      return ret;
    }
    haveVideo = true;
    if (pImpl->codecs[streamIndex]->img_convert_ctx != NULL) {
//      AVPicture avPicture;

      int result =
          sws_scale(pImpl->codecs[streamIndex]->img_convert_ctx,
                    avFrame->data,
                    avFrame->linesize,
                    0,
                    avFrame->height,
                    pImpl->codecs[streamIndex]->convertFrame->data,
                    pImpl->codecs[streamIndex]->convertFrame->linesize);

      av_frame_free(&avFrame);
      outFrame = pImpl->codecs[streamIndex]->convertFrame;

    } else {
      outFrame = avFrame;
    }


    if (outputMode == 0) { // kOutputModeYuv
      jboolean initResult = env->CallBooleanMethod(
          jOutputBuffer, pImpl->initForYuvFrame, outFrame->width, outFrame->height,
          outFrame->linesize[0], outFrame->linesize[1], 0/*kColorspaceUnknown*/);
      if (env->ExceptionCheck() || !initResult) {
        return -1;
      }
      const jobject dataObject = env->GetObjectField(jOutputBuffer, pImpl->dataField);
      jbyte *const data = reinterpret_cast<jbyte *>(env->GetDirectBufferAddress(dataObject));

      const int32_t uvHeight = (outFrame->height + 1) / 2;
      const uint64_t yLength = outFrame->linesize[0] * outFrame->height;
      const uint64_t uLength = outFrame->linesize[1] * uvHeight;
      const uint64_t vLength = outFrame->linesize[2] * uvHeight;
      // TODO: This copy can be eliminated by using external frame
      // buffers. This is insignificant for smaller videos but takes ~1.5ms
      // for 1080p clips. So this should eventually be gotten rid of.
      memcpy(data, outFrame->data[0], yLength);
      memcpy(data + yLength, outFrame->data[1], uLength);
      memcpy(data + yLength + uLength, outFrame->data[2], vLength);
      av_frame_free(&outFrame);
    } else if (outputMode == 1) { // kOutputModeSurfaceYuv
      int frameId = pImpl->bufferManager->set_buffer(outFrame->data,
                                                     outFrame->linesize,
                                                     outFrame->width,
                                                     outFrame->height);
      if (frameId < 0) {
        return -1;
      }
      env->CallVoidMethod(jOutputBuffer,
                          pImpl->initForPrivateFrame,
                          outFrame->width,
                          outFrame->height);
      if (pImpl->codecs[streamIndex]->img_convert_ctx == NULL) {
        av_frame_free(&outFrame);
      }

      if (env->ExceptionCheck()) {
        return -1;
      }
      env->SetIntField(jOutputBuffer, pImpl->decoderPrivateField,
                       frameId + KEY_DECODING_PRIVATE_BASE);
    }

  }
  return 0;
}


static void acquire_native_window(FFMKVDecoderImpl *pImpl, JNIEnv *env, jobject new_surface) {
  if (pImpl->surface != new_surface) {
    if (pImpl->native_window) {
      ANativeWindow_release(pImpl->native_window);
    }
    pImpl->native_window = ANativeWindow_fromSurface(env, new_surface);
    pImpl->surface = new_surface;
  }
}

constexpr int AlignTo16(int value) { return (value + 15) & (~15); }

VIDEO_DECODER_FUNC(jint,
                   FfmpegRenderFrame,
                   jlong context,
                   jint streamIndex,
                   jobject jSurface,
                   jobject jOutputBuffer) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return -1;

  const int
      id = env->GetIntField(jOutputBuffer, pImpl->decoderPrivateField) - KEY_DECODING_PRIVATE_BASE;
  if (id < 0) {
    return -1;
  }
  JniFrameBuffer *srcBuffer = pImpl->bufferManager->get_buffer(id);
  if (srcBuffer == NULL)
    return -1;
  acquire_native_window(pImpl, env, jSurface);

  if (pImpl->native_window == NULL || !srcBuffer) {
    return 1;
  }
  static const int kImageFormatYV12 = 0x32315659;
  if (pImpl->width != srcBuffer->width || pImpl->height != srcBuffer->height) {
    ANativeWindow_setBuffersGeometry(pImpl->native_window, srcBuffer->width,
                                     srcBuffer->height, kImageFormatYV12);
    pImpl->width = srcBuffer->width;
    pImpl->height = srcBuffer->height;
  }

  ANativeWindow_Buffer buffer;
  int result = ANativeWindow_lock(pImpl->native_window, &buffer, NULL);
  if (buffer.bits == NULL || result) {
    return -1;
  }

  // Y
  const size_t src_y_stride = srcBuffer->linesizes[0];
  int stride = srcBuffer->width;
  const uint8_t *src_base = srcBuffer->pointers[0];
  uint8_t *dest_base = (uint8_t *) buffer.bits;
  for (int y = 0; y < srcBuffer->height; y++) {
    memcpy(dest_base, src_base, stride);
    src_base += src_y_stride;
    dest_base += buffer.stride;
  }

  const int y_plane_size =
      buffer.stride * buffer.height;
  const int32_t native_window_buffer_uv_height =
      (buffer.height + 1) / 2;
  const int native_window_buffer_uv_stride =
      AlignTo16(buffer.stride / 2);

  int harfHeight = srcBuffer->height / 2;
  const int v_plane_height =
      native_window_buffer_uv_height > harfHeight ? harfHeight : native_window_buffer_uv_height;
  const uint8_t *src_base_v = srcBuffer->pointers[2];
  uint8_t *dest_v_base = (uint8_t *) buffer.bits + y_plane_size;
  for (int y = 0; y < v_plane_height; y++) {
    memcpy(dest_v_base, src_base_v, stride);
    src_base_v += srcBuffer->linesizes[2];
    dest_v_base += native_window_buffer_uv_stride;
  }
  const int v_plane_size = v_plane_height * native_window_buffer_uv_stride;
  uint8_t *dest_u_base = (uint8_t *) buffer.bits + y_plane_size + v_plane_size;
  const uint8_t *src_base_u = srcBuffer->pointers[1];
  for (int y = 0; y < v_plane_height; y++) {
    memcpy(dest_u_base, src_base_u, stride);
    src_base_u += srcBuffer->linesizes[1];
    dest_u_base += native_window_buffer_uv_stride;
  }


  return ANativeWindow_unlockAndPost(pImpl->native_window);
}

VIDEO_DECODER_FUNC(void,
                   FfmpegReleaseFrame,
                   jlong context,
                   jint streamIndex,
                   jobject jOutputBuffer) {
  FFMKVDecoderImpl *pImpl = (FFMKVDecoderImpl *) context;
  if (pImpl == NULL)
    return;

  const int
      id = env->GetIntField(jOutputBuffer, pImpl->decoderPrivateField) - KEY_DECODING_PRIVATE_BASE;
  env->SetIntField(jOutputBuffer, pImpl->decoderPrivateField, -1);
  pImpl->bufferManager->release(id);
}


AVCodec *getCodecByName(JNIEnv *env, jstring codecName) {
  if (!codecName) {
    return NULL;
  }
  const char *codecNameChars = env->GetStringUTFChars(codecName, NULL);
  AVCodec *codec = avcodec_find_decoder_by_name(codecNameChars);
  env->ReleaseStringUTFChars(codecName, codecNameChars);
  return codec;
}

AVCodecContext *createContext(JNIEnv *env, AVCodec *codec, jbyteArray extraData,
                              jboolean outputFloat, jint rawSampleRate,
                              jint rawChannelCount) {
  AVCodecContext *context = avcodec_alloc_context3(codec);
  if (!context) {
    LOGE("Failed to allocate context.");
    return NULL;
  }
  context->request_sample_fmt =
      outputFloat ? OUTPUT_FORMAT_PCM_FLOAT : OUTPUT_FORMAT_PCM_16BIT;
  if (extraData) {
    jsize size = env->GetArrayLength(extraData);
    context->extradata_size = size;
    context->extradata =
        (uint8_t *) av_malloc(size + AV_INPUT_BUFFER_PADDING_SIZE);
    if (!context->extradata) {
      LOGE("Failed to allocate extradata.");
      releaseContext(context);
      return NULL;
    }
    env->GetByteArrayRegion(extraData, 0, size, (jbyte *) context->extradata);
  }
  if (context->codec_id == AV_CODEC_ID_PCM_MULAW ||
      context->codec_id == AV_CODEC_ID_PCM_ALAW) {
    context->sample_rate = rawSampleRate;
    context->channels = rawChannelCount;
    context->channel_layout = av_get_default_channel_layout(rawChannelCount);
  }
  context->err_recognition = AV_EF_IGNORE_ERR;
  int result = avcodec_open2(context, codec, NULL);
  if (result < 0) {
    logError("avcodec_open2", result);
    releaseContext(context);
    return NULL;
  }
  return context;
}

int decodePacket(AVCodecContext *context, AVPacket *packet,
                 uint8_t *outputBuffer, int outputSize) {
  int result = 0;
  // Queue input data.
  result = avcodec_send_packet(context, packet);
  if (result) {
    logError("avcodec_send_packet", result);
    return result == AVERROR_INVALIDDATA ? AUDIO_DECODER_ERROR_INVALID_DATA
                                         : AUDIO_DECODER_ERROR_OTHER;
  }

  // Dequeue output data until it runs out.
  int outSize = 0;
  while (true) {
    AVFrame *frame = av_frame_alloc();
    if (!frame) {
      LOGE("Failed to allocate output frame.");
      return -1;
    }
    result = avcodec_receive_frame(context, frame);
    if (result) {
      av_frame_free(&frame);
      if (result == AVERROR(EAGAIN)) {
        break;
      }
      logError("avcodec_receive_frame", result);
      return result;
    }

    // Resample output.
    AVSampleFormat sampleFormat = context->sample_fmt;
    int channelCount = context->channels;
    int channelLayout = context->channel_layout;
    int sampleRate = context->sample_rate;
    int sampleCount = frame->nb_samples;
    int dataSize = av_samples_get_buffer_size(NULL, channelCount, sampleCount,
                                              sampleFormat, 1);
    SwrContext *resampleContext;
    if (context->opaque) {
      resampleContext = (SwrContext *) context->opaque;
    } else {
      resampleContext = swr_alloc();
      av_opt_set_int(resampleContext, "in_channel_layout", channelLayout, 0);
      av_opt_set_int(resampleContext, "out_channel_layout", channelLayout, 0);
      av_opt_set_int(resampleContext, "in_sample_rate", sampleRate, 0);
      av_opt_set_int(resampleContext, "out_sample_rate", sampleRate, 0);
      av_opt_set_int(resampleContext, "in_sample_fmt", sampleFormat, 0);
      // The output format is always the requested format.
      av_opt_set_int(resampleContext, "out_sample_fmt",
                     context->request_sample_fmt, 0);
      result = swr_init(resampleContext);
      if (result < 0) {
        logError("swr_init", result);
        av_frame_free(&frame);
        return -1;
      }
      context->opaque = resampleContext;
    }
    int inSampleSize = av_get_bytes_per_sample(sampleFormat);
    int outSampleSize = av_get_bytes_per_sample(context->request_sample_fmt);
    int outSamples = swr_get_out_samples(resampleContext, sampleCount);
    int bufferOutSize = outSampleSize * channelCount * outSamples;
    if (outSize + bufferOutSize > outputSize) {
      LOGE("Output buffer size (%d) too small for output data (%d).",
           outputSize, outSize + bufferOutSize);
      av_frame_free(&frame);
      return -1;
    }
    result = swr_convert(resampleContext, &outputBuffer, bufferOutSize,
                         (const uint8_t **) frame->data, frame->nb_samples);
    av_frame_free(&frame);
    if (result < 0) {
      logError("swr_convert", result);
      return result;
    }
    int available = swr_get_out_samples(resampleContext, 0);
    if (available != 0) {
      LOGE("Expected no samples remaining after resampling, but found %d.",
           available);
      return -1;
    }
    outputBuffer += bufferOutSize;
    outSize += bufferOutSize;
  }
  return outSize;
}

void logError(const char *functionName, int errorNumber) {
  char *buffer = (char *) malloc(ERROR_STRING_BUFFER_LENGTH * sizeof(char));
  av_strerror(errorNumber, buffer, ERROR_STRING_BUFFER_LENGTH);
  LOGE("Error in %s: %s", functionName, buffer);
  free(buffer);
}

void releaseContext(AVCodecContext *context) {
  if (!context) {
    return;
  }
  SwrContext *swrContext;
  if ((swrContext = (SwrContext *) context->opaque)) {
    swr_free(&swrContext);
    context->opaque = NULL;
  }
  avcodec_free_context(&context);
}


int open_codec_context(int *stream_idx,
                       AVCodecContext **dec_ctx, AVFormatContext *fmt_ctx, enum AVMediaType type) {
  int ret, stream_index;
  AVStream *st;
  AVCodec *dec = NULL;
  AVDictionary *opts = NULL;

  ret = av_find_best_stream(fmt_ctx, type, -1, -1, NULL, 0);
  if (ret < 0) {
    LOGE("Could not find %s stream'\n",
         av_get_media_type_string(type));
    return ret;
  } else {
    stream_index = ret;
    st = fmt_ctx->streams[stream_index];

    /* find decoder for the stream */
    dec = avcodec_find_decoder(st->codecpar->codec_id);
    if (!dec) {
      LOGE("Failed to find %s codec\n",
           av_get_media_type_string(type));
      return AVERROR(EINVAL);
    }

    /* Allocate a codec context for the decoder */
    *dec_ctx = avcodec_alloc_context3(dec);
    if (!*dec_ctx) {
      LOGE("Failed to allocate the %s codec context\n",
           av_get_media_type_string(type));
      return AVERROR(ENOMEM);
    }

    /* Copy codec parameters from input stream to output codec context */
    if ((ret = avcodec_parameters_to_context(*dec_ctx, st->codecpar)) < 0) {
      LOGE("Failed to copy %s codec parameters to decoder context\n",
           av_get_media_type_string(type));
      return ret;
    }

    /* Init the decoders */
    if ((ret = avcodec_open2(*dec_ctx, dec, &opts)) < 0) {
      LOGE("Failed to open %s codec\n",
           av_get_media_type_string(type));
      return ret;
    }
    *stream_idx = stream_index;
  }

  return 0;
}


