JavaCV
Pros: JavaCV is the most powerful option because it uses native FFmpeg and OpenCV underneath. That means it runs fast, supports advanced features like GPU acceleration, and has a big active community.

Cons: It comes with a lot of native binaries, so the project size gets pretty large. Since it relies on native code, there is also a higher chance of hard crashes if memory handling goes wrong.

JCodec
Pros: JCodec is fully written in Java, so it is easy to set up, portable, and does not require native installs. It is also safer in the sense that native-level crashes are much less of a concern.

Cons: It is slower than native-based tools, and it does not keep up as well with modern codecs like H.265 and AV1.

IVCompressor
Pros: IVCompressor is very beginner-friendly and focused on simple compression workflows. You can do basic compression and conversion with very little code.

Cons: It is limited if you need deeper control, especially for frame-by-frame work or direct pixel-level processing.