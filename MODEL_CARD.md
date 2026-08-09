# Model Card: Fashion YOLO Nano

On-device fashion object detection model fine-tuned for Aura.

## Model Details

- **Model Name:** Fashion YOLO Nano (Ultralytics YOLOv8n fine-tuned on Fashionpedia)
- **Model Source:** [louisJLN/yolo8-fashionpedia](https://huggingface.co/louisJLN/yolo8-fashionpedia)
- **Dataset:** Fashionpedia
- **License:** MIT
- **Model Size:** 11.7 MB (12,273,953 bytes)
- **Format:** ONNX Runtime (.onnx)

## Model Contract

### Input Spec
- **Name:** `images`
- **Shape:** `1 x 3 x 640 x 640`
- **Data Type:** `FLOAT32` (elem_type: 1)
- **Layout:** NCHW (Batch, Channels, Height, Width)
- **Color Order:** RGB
- **Normalization:** Values scaled to `[0.0, 1.0]` (no additional mean/std subtraction required)

### Output Spec
- **Name:** `output0`
- **Shape:** `1 x 50 x 8400`
- **Data Type:** `FLOAT32` (elem_type: 1)
- **Format:** Raw YOLOv8 output containing `50` attributes for `8400` anchor boxes.
  - First 4 rows: `[x_center, y_center, width, height]` coordinates in the 640x640 space.
  - Next 46 rows: Class confidence probabilities for each of the 46 fashion classes.

---

## Classes (46 total)

0. `shirt, blouse`
1. `top, t-shirt, sweatshirt`
2. `sweater`
3. `cardigan`
4. `jacket`
5. `vest`
6. `pants`
7. `shorts`
8. `skirt`
9. `coat`
10. `dress`
11. `jumpsuit`
12. `cape`
13. `glasses`
14. `hat`
15. `headband, head covering, hair accessory`
16. `tie`
17. `glove`
18. `watch`
19. `belt`
20. `leg warmer`
21. `tights, stockings`
22. `sock`
23. `shoe`
24. `bag, wallet`
25. `scarf`
26. `umbrella`
27. `hood`
28. `collar`
29. `lapel`
30. `epaulette`
31. `sleeve`
32. `pocket`
33. `neckline`
34. `buckle`
35. `zipper`
36. `applique`
37. `bead`
38. `bow`
39. `flower`
40. `fringe`
41. `ribbon`
42. `rivet`
43. `ruffle`
44. `sequin`
45. `tassel`

---

## Preprocessing Pipeline

1. **Orientation Correction:** Bitmap rotation if EXIF tag indicates rotation.
2. **Letterbox Resize:** Resize Bitmap to `640x640` preserving aspect ratio using padding.
3. **RGB Conversion:** Read pixel values and extract R, G, B channels.
4. **Scale & Normalize:** Scale values to `[0.0, 1.0]`.
5. **Planar HWC to CHW Conversion:** Write to direct float buffer in NCHW planar layout.

---

## Postprocessing Pipeline

1. **Threshold Filtering:** Filter anchors with confidence >= `0.35`.
2. **Coordinate Restoration (Undo Letterbox):**
   - Scale factor: `scale = min(640 / srcWidth, 640 / srcHeight)`
   - X offset: `dx = (640 - srcWidth * scale) / 2`
   - Y offset: `dy = (640 - srcHeight * scale) / 2`
   - Map center coordinates back:
     - `x_orig = (x_640 - dx) / scale`
     - `y_orig = (y_640 - dy) / scale`
3. **NMS (Non-Maximum Suppression):** Class-specific NMS with IoU threshold of `0.45`.

---

## Device Performance & Limitations

- **CPU Inference Speed:** ~150ms to ~350ms depending on Android device specs.
- **Memory Footprint:** Reuse single `OrtSession` to avoid recreating it repeatedly. Keep intermediate bitmaps and float buffers short-lived.
- **Known Limitations:** Detection of small objects (e.g. rivets, beads, tassels) is less confident compared to outer garments (e.g. jackets, pants, dresses). Overlapping clothing articles of the same class may get suppressed.
