package ru.netology.test;

import dev.failsafe.internal.util.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.data.DataHelper;
import ru.netology.page.DashboardPage;
import ru.netology.page.LoginPageV2;


import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.data.DataHelper.*;

class MoneyTransferTest {
    DashboardPage dashboardPage;
    DataHelper.CardInfo firstCardInfo;
    DataHelper.CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach
    void setup(){
        var loginPage = open("http://localhost:9999", LoginPageV2.class);
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = getVerificationCode();
        dashboardPage = verificationPage.validVerify(verificationCode);
        firstCardInfo = getFirstCardInfo();
        secondCardInfo = getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(firstCardInfo);
        secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
    }

    @Test
    void shouldTransferFromFirstToSecond() {
      var amount = generateValidAmount(firstCardBalance);
      var expectedBalanceFirstCard = firstCardBalance - amount;
      var expectedBalanceSecondCard = secondCardBalance + amount;
      var transferPage = dashboardPage.selectCardTransfer(secondCardInfo);
      dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount),firstCardInfo);
      var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
      var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
      assertAll(() -> assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard),
              () -> assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard));
    }

    @Test
    void shouldGetErrorMessageIfAmountMoreBalance() {
        var amount = generateInvalidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCardTransfer(firstCardInfo);
        transferPage.makeTransfer(String.valueOf(amount),firstCardInfo);
        transferPage.findErrorMessage("Выполнена попытка перевода суммы, превышающей остаток на карте списания");
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertAll(() -> assertEquals(firstCardBalance, actualBalanceFirstCard),
                () -> assertEquals(secondCardBalance, actualBalanceSecondCard));
    }
}

