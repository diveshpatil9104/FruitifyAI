package com.example.fruitifyai.classifier

import android.content.Context
import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream

class FruitClassifier(context: Context) {

    private val model: Module = Module.load(assetFilePath(context, "fruits_vegetables_51.pt"))

    private val classNames = listOf(
        "Amaranth", "Apple", "Banana", "Beetroot", "Bell pepper", "Bitter Gourd", "Blueberry",
        "Bottle Gourd", "Broccoli", "Cabbage", "Cantaloupe", "Capsicum", "Carrot", "Cauliflower",
        "Chilli pepper", "Coconut", "Corn", "Cucumber", "Dragon_fruit", "Eggplant", "Fig",
        "Garlic", "Ginger", "Grapes", "Jalepeno", "Kiwi", "Lemon", "Mango", "Okra", "Onion",
        "Orange", "Paprika", "Pear", "Peas", "Pineapple", "Pomegranate", "Potato", "Pumpkin",
        "Raddish", "Raspberry", "Ridge Gourd", "Soy beans", "Spinach", "Spiny Gourd", "Sponge Gourd",
        "Strawberry", "Sweetcorn", "Sweetpotato", "Tomato", "Turnip", "Watermelon"
    )

    fun predict(bitmap: Bitmap): String {
        return predictWithConfidence(bitmap).first
    }

    fun predictWithConfidence(bitmap: Bitmap): Pair<String, Float> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            resizedBitmap,
            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            TensorImageUtils.TORCHVISION_NORM_STD_RGB
        )
        val outputTensor = model.forward(IValue.from(inputTensor)).toTensor()
        val logits = outputTensor.dataAsFloatArray
        val probabilities = softmax(logits)
        val maxIdx = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

        val label = if (maxIdx in classNames.indices) classNames[maxIdx] else "Unknown"
        val confidence = if (maxIdx != -1) probabilities[maxIdx] else 0f
        return Pair(label, confidence)
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val exp = logits.map { Math.exp((it - maxLogit).toDouble()) }
        val sumExp = exp.sum()
        return exp.map { (it / sumExp).toFloat() }.toFloatArray()
    }

    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }
        return file.absolutePath
    }
}