package org.lasantha.mycalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private val maxLen = 13
    private val df = DecimalFormat("#.##########")

    private var bufOperand = ""
    private var bufOperandOther = ""
    private var bufOperator = ""
    private var state = State.BEGINING

    private var txtMain: TextView? = null
    private var txtOther: TextView? = null
    private var txtOperator: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtMain = findViewById(R.id.txtMain)
        txtOther = findViewById(R.id.txtOther)
        txtOperator = findViewById(R.id.txtOperator)

        clear()
        paintText()
        createButtonClickListeners()
    }

    private fun createButtonClickListeners() {
        findViewById<Button>(R.id.btnNum1).setOnClickListener(clickListener(ButtonTypes.NUM1))
        findViewById<Button>(R.id.btnNum2).setOnClickListener(clickListener(ButtonTypes.NUM2))
        findViewById<Button>(R.id.btnNum3).setOnClickListener(clickListener(ButtonTypes.NUM3))
        findViewById<Button>(R.id.btnNum4).setOnClickListener(clickListener(ButtonTypes.NUM4))
        findViewById<Button>(R.id.btnNum5).setOnClickListener(clickListener(ButtonTypes.NUM5))
        findViewById<Button>(R.id.btnNum6).setOnClickListener(clickListener(ButtonTypes.NUM6))
        findViewById<Button>(R.id.btnNum7).setOnClickListener(clickListener(ButtonTypes.NUM7))
        findViewById<Button>(R.id.btnNum8).setOnClickListener(clickListener(ButtonTypes.NUM8))
        findViewById<Button>(R.id.btnNum9).setOnClickListener(clickListener(ButtonTypes.NUM9))
        findViewById<Button>(R.id.btnNum0).setOnClickListener(clickListener(ButtonTypes.NUM0))

        findViewById<Button>(R.id.btnOpAdd).setOnClickListener(clickListener(ButtonTypes.OP_ADD))
        findViewById<Button>(R.id.btnOpSubtract).setOnClickListener(clickListener(ButtonTypes.OP_SUBTRACT))
        findViewById<Button>(R.id.btnOpDivide).setOnClickListener(clickListener(ButtonTypes.OP_DIVIDE))
        findViewById<Button>(R.id.btnOpMultiply).setOnClickListener(clickListener(ButtonTypes.OP_MULTIPLY))

        findViewById<Button>(R.id.btnPeriod).setOnClickListener(clickListener(ButtonTypes.PERIOD))
        findViewById<Button>(R.id.btnSign).setOnClickListener(clickListener(ButtonTypes.SIGN))
        findViewById<Button>(R.id.btnClear).setOnClickListener(clickListener(ButtonTypes.CLEAR))
        findViewById<Button>(R.id.btnClearEntry).setOnClickListener(clickListener(ButtonTypes.CLEAR_ENTRY))
        findViewById<Button>(R.id.btnEqual).setOnClickListener(clickListener(ButtonTypes.EQUAL))
    }

    private val clickListener: (ButtonTypes) -> (View?) -> Unit = { btnType ->
        viewHandler@{ _ ->

            if (btnType == ButtonTypes.CLEAR) {
                clear() // State.BEGINING
                paintText()
                return@viewHandler
            } else if (btnType == ButtonTypes.CLEAR_ENTRY) {
                clearEntry() // State.BEGINING
                paintText()
                return@viewHandler
            }

            val str = strBtn(btnType)
            when (btnType) {
                ButtonTypes.NUM1, ButtonTypes.NUM2, ButtonTypes.NUM3,
                ButtonTypes.NUM4, ButtonTypes.NUM5, ButtonTypes.NUM6,
                ButtonTypes.NUM7, ButtonTypes.NUM8, ButtonTypes.NUM9 -> {
                    when (state) {
                        State.BEGINING -> {
                            state = State.OPERAND
                            bufOperand = str
                        }
                        State.OPERAND -> setKeyText(str, append = true)
                    }
                }
                ButtonTypes.NUM0 -> {
                    when (state) {
                        State.BEGINING -> setKeyText(str)
                        State.OPERAND -> setKeyText(str, append = true)
                    }
                }
                ButtonTypes.PERIOD -> {
                    when (state) {
                        State.BEGINING -> {
                            clearEntry()
                            state = State.OPERAND
                            setKeyText(".", append = true)
                        }
                        State.OPERAND -> {
                            if (!bufOperand.contains(".")) {
                                setKeyText(".", append = true)
                            }
                        }
                    }
                }
                ButtonTypes.SIGN -> {
                    when (state) {
                        State.OPERAND -> {
                            bufOperand = if (bufOperand.startsWith('-')) bufOperand.substring(1)
                            else "-$bufOperand"
                        }
                        else -> Unit
                    }
                }
                ButtonTypes.OP_ADD, ButtonTypes.OP_SUBTRACT,
                ButtonTypes.OP_MULTIPLY, ButtonTypes.OP_DIVIDE -> {
                    bufOperandOther = validateInputText(bufOperand)
                    bufOperator = str
                    clearEntry() // State.BEGINING
                }
                ButtonTypes.EQUAL -> {
                    try {
                        val result = calculate()
                        clear() // State.BEGINING
                        bufOperand = result
                    } catch (e: ArithmeticException) {
                        clear() // State.BEGINING
                        Toast.makeText(
                            this, "Error: ${e.message}", Toast.LENGTH_LONG
                        ).show()
                    }
                }
                else -> Unit // Do nothing, stay in the same sate
            }

            paintText()
        }
    }

    private fun strBtn(type: ButtonTypes): String = getString(type.resourceId)

    private fun paintText() {
        txtMain?.let { it.text = bufOperand }
        txtOther?.let { it.text = bufOperandOther }
        txtOperator?.let { it.text = bufOperator }
    }

    private fun clear() {
        bufOperand = "0"
        bufOperandOther = ""
        bufOperator = ""
        state = State.BEGINING
    }

    private fun clearEntry() {
        bufOperand = "0"
        state = State.BEGINING
    }

    private fun setKeyText(char: String, append: Boolean = false) {
        if (!append) {
            bufOperand = char
        } else if (bufOperand.length < maxLen) {
            bufOperand += char
        }
    }

    private fun validateInputText(s: String): String {
        return df.format(s.toDouble())
    }

    private fun calculate(): String {
        if (bufOperandOther.isNotEmpty() && bufOperand.isNotEmpty() && bufOperator.isNotEmpty()) {
            val a = bufOperandOther.toDouble()
            val b = bufOperand.toDouble()
            val c = when (bufOperator) {
                strBtn(ButtonTypes.OP_ADD) -> a + b
                strBtn(ButtonTypes.OP_SUBTRACT) -> a - b
                strBtn(ButtonTypes.OP_MULTIPLY) -> a * b
                strBtn(ButtonTypes.OP_DIVIDE) -> a / b
                else -> throw ArithmeticException("Invalid operator: $bufOperator")
            }

            if (c.isNaN()) throw ArithmeticException("Not a number")
            if (c.isInfinite()) throw ArithmeticException("Infinite")

            return df.format(c)
        }
        return "0"
    }

    private enum class State { BEGINING, OPERAND }
}