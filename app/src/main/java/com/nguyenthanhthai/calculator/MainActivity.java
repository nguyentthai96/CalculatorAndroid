package com.nguyenthanhthai.calculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    TextView textViewExpression;
    //Chuỗi vẫn chứa các toán tử +,-, *, / để dễ dàng thực hiện tính toán
    String expression;
    private boolean checkResult = false;

    //Xóa một ký tự cuối của chuỗi expression
    private void backSpaceChar() {
        expression = expression.substring(0, expression.length() - 1);
    }

    //So sánh ký tự ch với ký tự cuối expression Nếu expression rỗng return false
    private boolean compareToCharLast(char ch) {
        if (expression.isEmpty())
            return false;
        return (expression.charAt(expression.length() - 1) == ch);
    }

    Button buttonNeg, buttonBraket;
    boolean negavitionOpen = false;
    int braketOpen = 0;
    boolean dotFloat = false; //bằng true khi vừa nhấn . chưa thoát khỏi số, cho phép nhập . khi dotFloat = flase

    //trạng thái các nút ấn
    enum EnumStatus {
        NumberClick, DotClick, Negativite, Bracket, OperatorClick
    }

    ;

    //stack nút ấn
    Stack<EnumStatus> statusBeforClick;

    Button buttonMul, buttonDiv, buttonAdd, buttonSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().hide();

        statusBeforClick = new Stack<EnumStatus>();
        expression = "";
        braketOpen = 0;
        addControls();
        addEvents();
    }

    private void addControls() {
        textViewExpression = (TextView) findViewById(R.id.textViewExpression);
        textViewExpression.setMovementMethod(new ScrollingMovementMethod());
        textViewExpression.setSelected(true);
        buttonBraket = (Button) findViewById(R.id.buttonBraket);
        buttonNeg = (Button) findViewById(R.id.buttonNeg);

        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonSub = (Button) findViewById(R.id.buttonSub);
        buttonMul = (Button) findViewById(R.id.buttonMul);
        buttonDiv = (Button) findViewById(R.id.buttonDiv);
    }

    private void addEvents() {
        //Kiểm tra xử lý ()
        buttonBraket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishResulClick();
                if (!statusBeforClick.isEmpty()) {
                    if (statusBeforClick.peek() == EnumStatus.DotClick) {
                        backSpaceChar();
                        statusBeforClick.pop();

                        expression += "*(";
                        braketOpen++;
                        statusBeforClick.push(EnumStatus.OperatorClick);
                        statusBeforClick.push(EnumStatus.Bracket);
                    } else if (statusBeforClick.peek() == EnumStatus.OperatorClick || compareToCharLast('(')) {
                        expression += "(";
                        braketOpen++;
                        statusBeforClick.push(EnumStatus.Bracket);
                    } else if ((statusBeforClick.peek() == EnumStatus.NumberClick
                            || compareToCharLast(')'))
                            && braketOpen == 0) {
                        expression += "*(";
                        braketOpen++;
                        statusBeforClick.push(EnumStatus.OperatorClick);
                        statusBeforClick.push(EnumStatus.Bracket);
                    } else {
                        if (braketOpen > 0) {
                            if (statusBeforClick.peek() == EnumStatus.Negativite) {
                                expression += "(";
                                braketOpen++;
                                statusBeforClick.push(EnumStatus.Bracket);
                            } else {
                                expression += ")";
                                braketOpen--;
                                statusBeforClick.push(EnumStatus.Bracket);
                            }
                        }
                    }
                } else {
                    expression += "(";
                    braketOpen++;
                    statusBeforClick.push(EnumStatus.Bracket);
                }
                dotFloat = false; //cho phép lại nhập .
                viewExpression();
            }
        });

        //Kiểm tra xử lý nút +/-
        buttonNeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!statusBeforClick.isEmpty()) {
                    if (statusBeforClick.peek() == EnumStatus.DotClick
                            || statusBeforClick.peek() == EnumStatus.NumberClick) {
                        repairNumber();
                        return;
                    }

                    if (statusBeforClick.peek() == EnumStatus.Negativite) {
                        backSpaceChar();
                        backSpaceChar();
                        braketOpen--;
                        statusBeforClick.pop();
                        viewExpression();
                        return;
                    }
                }

                if (compareToCharLast(')')) {
                    expression += "*";
                    statusBeforClick.push(EnumStatus.OperatorClick);
                }

                expression += "(-";
                negavitionOpen = true;
                braketOpen++;
                statusBeforClick.push(EnumStatus.Negativite);
                viewExpression();
            }
        });

        //Sự kiện ấn nút cho các toán tử +, -, *, /
        View.OnClickListener operatorClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishResulClick();
                if (compareToCharLast('(')) {
                    return;
                }
                //Xử lý đóng ngoặc cho +/-
                if (negavitionOpen == true) {
                    if (statusBeforClick.peek() == EnumStatus.Negativite) {
                        backSpaceChar();
                        statusBeforClick.pop();
                        statusBeforClick.push(EnumStatus.Bracket);
                        dotFloat = false; //cho phép lại nhập .
                        viewExpression();
                        return;
                    } else {
                        expression += ")"; //đóng ngoặc tự động cho toán tử +/-
                        negavitionOpen = false;
                        braketOpen--;
                        statusBeforClick.push(EnumStatus.Bracket);
                    }
                }

                if (R.id.buttonSub == v.getId()) {
                    subtract();
                } else if (R.id.buttonAdd == v.getId()) {
                    add();
                } else if (R.id.buttonMul == v.getId()) {
                    multi();
                } else if (R.id.buttonDiv == v.getId()) {
                    div();
                }
                dotFloat = false; //cho phép lại nhập .
                viewExpression();
            }
        };

        buttonAdd.setOnClickListener(operatorClick);
        buttonSub.setOnClickListener(operatorClick);
        buttonMul.setOnClickListener(operatorClick);
        buttonDiv.setOnClickListener(operatorClick);
    }

    //Khi là một con số hoặc một . mà ấn +/-
    private void repairNumber() {
        if (negavitionOpen == true) {//delete dấu (-
            statusBeforClick.remove(statusBeforClick.lastIndexOf(EnumStatus.Negativite));
            int indexNeg = expression.lastIndexOf("(-");
            expression = expression.substring(0, indexNeg) + expression.substring(indexNeg + 2);
            braketOpen--;
        } else {
            //Tìm vị trí cuối cùng mà có dấu ( (cũng tức là dấu ngoặc vì sẽ không có dấu đóng ngay cuối là số) hoặc toán tử
            int indexBracket = statusBeforClick.lastIndexOf(EnumStatus.Bracket);
            int indexOper = statusBeforClick.lastIndexOf(EnumStatus.OperatorClick);
            if (indexBracket == -1 && indexOper == -1) {
                statusBeforClick.add(0, EnumStatus.Negativite);
                expression = "(-" + expression;
            } else if (indexBracket > indexOper) {//chèn dấu (- sau dấu (
                statusBeforClick.add(indexBracket + 1, EnumStatus.Negativite);
                expression = expression.substring(0, indexBracket + 1) + "(-" + expression.substring(indexBracket + 1);
            } else {
                statusBeforClick.add(indexOper + 1, EnumStatus.Negativite);
                expression = expression.substring(0, indexOper + 1) + "(-" + expression.substring(indexOper + 1);
            }
            //do thêm dấu (- nên có một dấu ( tăng thêm
            braketOpen++;
        }
        negavitionOpen = !negavitionOpen;
        viewExpression();
    }

    //Kiểm tra các phép toán +, -, *, /
    private void div() {
        replaceDouble();
        if (expression.isEmpty()) {
            viewError("Bạn không thể nhập toán tử chia khi chưa có toán hạng");
            return;
        }
        expression += "/";
        statusBeforClick.push(EnumStatus.OperatorClick);
    }

    private void multi() {
        replaceDouble();
        if (expression.isEmpty()) {
            viewError("Bạn không thể nhập toán tử nhân khi chưa có toán hạng");
            return;
        }

        expression += "*";
        statusBeforClick.push(EnumStatus.OperatorClick);
    }

    private void add() {
        replaceDouble();
        expression += "+";
        statusBeforClick.push(EnumStatus.OperatorClick);
    }

    private void subtract() {
        replaceDouble();
        expression += "-";
        statusBeforClick.push(EnumStatus.OperatorClick);
    }

    //Kiểm tra nếu trước đó vừa nhập vào nó sẽ hủy một cái (tức lập lại toán tử
    private void replaceDouble() {
        if (!statusBeforClick.isEmpty()) {
            if (statusBeforClick.peek() == EnumStatus.OperatorClick) {
                backSpaceChar();
                statusBeforClick.pop();
                return;
            }

            if (statusBeforClick.peek() == EnumStatus.DotClick) {
                backSpaceChar();
                statusBeforClick.pop();
                return;
            }

            if (statusBeforClick.peek() == EnumStatus.Negativite) {
                backSpaceChar();
                statusBeforClick.pop();
                return;
            }
        }
    }

    //Sự kiện ấn một số
    public void onClickNumber(View view) {
        finishResulClick();
        if (compareToCharLast(')')) {
            expression += "*";
            statusBeforClick.push(EnumStatus.OperatorClick);
        }
        expression += ((Button) view).getText();
        statusBeforClick.push(EnumStatus.NumberClick);
        viewExpression();
    }

    //Xử lý button .
    public void onClickDot(View view) {
        finishResulClick();
        if (compareToCharLast('.') || dotFloat == true) {
            return;
        }
        if (statusBeforClick.isEmpty() || statusBeforClick.peek() != EnumStatus.NumberClick) {
            if (!statusBeforClick.isEmpty()
                    && (statusBeforClick.peek() == EnumStatus.NumberClick || compareToCharLast(')'))) {
                expression += "*";
                statusBeforClick.push(EnumStatus.OperatorClick);
            }
            expression += "0.";
            statusBeforClick.push(EnumStatus.NumberClick);

        } else {
            expression += ".";
        }
        dotFloat = true;
        statusBeforClick.push(EnumStatus.DotClick);
        viewExpression();
    }

    //in thông báo lỗi
    private void viewError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    //ấn = xử lý kết quả
    public void onClickResualt(View view) {
        String temp = expression;
        Stack<EnumStatus> tempStatus = new Stack<EnumStatus>();
        tempStatus = statusBeforClick;
        checkResult = true;

        if (statusBeforClick.isEmpty()) {
            return;
        }

        boolean checkContinute = true;
        while (checkContinute) {
            checkContinute = false; //nếu không vào while con thì sẽ dừng
            //Xóa toán tử thừa
            if (statusBeforClick.peek() == EnumStatus.OperatorClick) {
                backSpaceChar();
                statusBeforClick.pop();
                checkContinute = true; //tồn tại một trường hợp có lỗi sẽ phải kiểm tra xác nhận
            } else if (statusBeforClick.peek() == EnumStatus.Negativite) {
                backSpaceChar();
                backSpaceChar(); //do (-
                braketOpen--;
                statusBeforClick.pop();
                checkContinute = true; //tồn tại một trường hợp có lỗi sẽ phải kiểm tra xác nhận
            }
            //Xóa thừa dấu ngoặc (
            while (compareToCharLast('(')) {
                backSpaceChar();
                statusBeforClick.pop();
                braketOpen--;
                checkContinute = true;
            }
        }

        //Đóng ngoặc kết thúc phép toán
        while (braketOpen > 0) {
            expression += ")";
            braketOpen--;
            statusBeforClick.push(EnumStatus.Bracket);
        }

        try {
            // Create an Expression (A class from exp4j library) // build.gradle compile 'net.objecthunter:exp4j:0.4.8'
            Expression expressionBuilder = new ExpressionBuilder(expression).build();
            // Calculate the result and display
            double result = expressionBuilder.evaluate();
            viewExpression(result);
        } catch (Exception e) {
            if (e.toString().compareTo("java.lang.ArithmeticException: Division by zero!") == 0)
                viewError("Không thể chia cho 0");
            else {
                viewError("Lỗi công thức không thể tính!!!");
                viewError("\n"+expression);
            }
            expression = temp;temp = temp.replace("/", "<font COLOR='BLUE'>" + "÷" + "</font>");
            temp = temp.replace("*", "<font COLOR='BLUE'>" + "x" + "</font>");
            temp = temp.replace("+", "<font COLOR='BLUE'>" + "+" + "</font>");
            temp = temp.replace("-", "<font COLOR='BLUE'>" + "-" + "</font>");
            tempStatus = statusBeforClick;
            return;
        }
    }

    //xử lý sau khi ấn finish click
    private void finishResulClick() {
        if (checkResult)
            resetAll();
        checkResult = false;
    }

    private void viewExpression(double result) {
        String temp = "";
        temp = expression;
        if (temp != null && !temp.isEmpty()) {

        }

        if ((long) result == result) {
            temp += "<br/><font COLOR='GREEN'>" + "= " + ((long) result) + "</font>";
        } else temp += "<br/><font COLOR='GREEN'>" + "= " + Double.toString(result) + "</font>";
        temp = temp.replace(".", ",");

//        if (expression.length() > 20)
//            textViewExpression.setTextSize(20);
//        else textViewExpression.setTextSize(30);
        textViewExpression.setText(Html.fromHtml(temp));
        textViewExpression.getSelectionEnd();
    }

    private void viewExpression() {
        textViewExpression.refreshDrawableState();
        String temp = new String();
        temp = expression;
        if (temp != null && !temp.isEmpty()) {
            temp = temp.replace("/", "<font COLOR='BLUE'>" + "÷" + "</font>");
            temp = temp.replace("*", "<font COLOR='BLUE'>" + "x" + "</font>");
            temp = temp.replace("+", "<font COLOR='BLUE'>" + "+" + "</font>");
            temp = temp.replace("-", "<font COLOR='BLUE'>" + "-" + "</font>");
        }
        temp = temp.replace(".", ",");
//        if (expression.length() > 20)
//            textViewExpression.setTextSize(20);
//        else textViewExpression.setTextSize(30);

        textViewExpression.setText(Html.fromHtml(temp));
        textViewExpression.getSelectionEnd();

    }

    public void onClickClear(View view) {
        resetAll();
    }

    //reset màn hình và giá trị các biến
    private void resetAll() {
        expression = "";
        statusBeforClick.clear();
        statusBeforClick = new Stack<EnumStatus>();
        braketOpen = 0;
        negavitionOpen = false;
        checkResult = false;
        textViewExpression.setText("");
        textViewExpression.refreshDrawableState();
    }

    public void onClickRemoveBackButton(View view) {
        checkResult = false;
        if (expression.isEmpty())
            return;
        EnumStatus temp = statusBeforClick.pop();
        if (temp == EnumStatus.Negativite) {
            backSpaceChar();
            braketOpen--;
            negavitionOpen = false;
        }
        if (temp == EnumStatus.Bracket) {
            if (compareToCharLast(')')) {
                braketOpen++;
            } else {
                braketOpen--;
            }
        }
        backSpaceChar();
        viewExpression();
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        savedInstanceState.putString("expression", expression);
        savedInstanceState.putBoolean ("checkResult", checkResult);
        savedInstanceState.putBoolean ("negavitionOpen", negavitionOpen);
        savedInstanceState.putInt ("braketOpen", braketOpen);
        savedInstanceState.putBoolean ("dotFloat", dotFloat);
        savedInstanceState.putSerializable ("statusBeforClick", statusBeforClick);

        super.onSaveInstanceState(savedInstanceState);
    }
    //onRestoreInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
         expression=savedInstanceState.getString("expression");
        checkResult = savedInstanceState.getBoolean("checkResult");
        negavitionOpen  = savedInstanceState.getBoolean("negavitionOpen");;
        braketOpen = savedInstanceState.getInt("braketOpen");
         dotFloat = savedInstanceState.getBoolean("dotFloat");
        statusBeforClick= (Stack<EnumStatus>) savedInstanceState.getSerializable("statusBeforClick");
        if (checkResult){
            viewExpression();
        }
        else viewExpression();
    }
}