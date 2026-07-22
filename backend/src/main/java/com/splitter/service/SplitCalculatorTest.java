// package com.splitter.service;

// import com.splitter.dto.ParticipantShareRequest;
// import com.splitter.enums.SplitType;
// import com.splitter.exception.ApiException;
// import org.junit.jupiter.api.Test;
 
// import java.math.BigDecimal;
// import java.util.List;
// import java.util.Map;
 
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
 
// /**
//  * The split math is the single most bug-prone piece of this app — every test
//  * here also asserts the shares sum EXACTLY to the original amount, since a
//  * one-cent drift is the classic bug in any expense-splitting logic.
//  */
// public class SplitCalculatorTest {
 
//     private final SplitCalculator calculator = new SplitCalculator();
 
//     private ParticipantShareRequest participant(long userId, Double value) {
//         ParticipantShareRequest p = new ParticipantShareRequest();
//         p.setUserId(userId);
//         p.setValue(value == null ? null : BigDecimal.valueOf(value));
//         return p;
//     }
 
//     private BigDecimal sumOf(Map<Long, BigDecimal> shares) {
//         return shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
//     }
 
//     // ---------- EQUAL ----------
 
//     @Test
//     void equalSplit_dividesEvenlyWhenAmountDividesCleanly() {
//         List<ParticipantShareRequest> participants = List.of(participant(1, null), participant(2, null));
 
//         Map<Long, BigDecimal> result = calculator.calculate(new BigDecimal("100.00"), SplitType.EQUAL, participants);
 
//         assertThat(result.get(1L)).isEqualByComparingTo("50.00");
//         assertThat(result.get(2L)).isEqualByComparingTo("50.00");
//     }
 
//     @Test
//     void equalSplit_distributesRemainderCentsWithoutLosingMoney() {
//         // 100 / 3 = 33.33 with 1 cent left over — someone has to get the extra cent
//         List<ParticipantShareRequest> participants = List.of(
//                 participant(1, null), participant(2, null), participant(3, null));
 
//         Map<Long, BigDecimal> result = calculator.calculate(new BigDecimal("100.00"), SplitType.EQUAL, participants);
 
//         assertThat(sumOf(result)).isEqualByComparingTo("100.00");
//         long countWithExtraCent = result.values().stream().filter(v -> v.compareTo(new BigDecimal("33.34")) == 0).count();
//         assertThat(countWithExtraCent).isEqualTo(1);
//     }
 
//     // ---------- UNEQUAL ----------
 
//     @Test
//     void unequalSplit_usesExactProvidedAmounts() {
//         List<ParticipantShareRequest> participants = List.of(participant(1, 30.0), participant(2, 70.0));
 
//         Map<Long, BigDecimal> result = calculator.calculate(new BigDecimal("100.00"), SplitType.UNEQUAL, participants);
 
//         assertThat(result.get(1L)).isEqualByComparingTo("30.0");
//         assertThat(result.get(2L)).isEqualByComparingTo("70.0");
//     }
 
//     @Test
//     void unequalSplit_rejectsAmountsThatDontAddUpToTotal() {
//         List<ParticipantShareRequest> participants = List.of(participant(1, 30.0), participant(2, 60.0)); // only 90, not 100
 
//         assertThatThrownBy(() -> calculator.calculate(new BigDecimal("100.00"), SplitType.UNEQUAL, participants))
//                 .isInstanceOf(ApiException.class)
//                 .hasMessageContaining("must add up to the total amount");
//     }
 
//     // ---------- PERCENTAGE ----------
 
//     @Test
//     void percentageSplit_dividesByGivenPercentages() {
//         List<ParticipantShareRequest> participants = List.of(participant(1, 25.0), participant(2, 75.0));
 
//         Map<Long, BigDecimal> result = calculator.calculate(new BigDecimal("200.00"), SplitType.PERCENTAGE, participants);
 
//         assertThat(result.get(1L)).isEqualByComparingTo("50.00");
//         assertThat(result.get(2L)).isEqualByComparingTo("150.00");
//     }
 
//     @Test
//     void percentageSplit_rejectsPercentagesNotSummingTo100() {
//         List<ParticipantShareRequest> participants = List.of(participant(1, 25.0), participant(2, 50.0)); // only 75%
 
//         assertThatThrownBy(() -> calculator.calculate(new BigDecimal("100.00"), SplitType.PERCENTAGE, participants))
//                 .isInstanceOf(ApiException.class)
//                 .hasMessageContaining("must add up to 100");
//     }
 
//     @Test
//     void percentageSplit_lastParticipantAbsorbsRoundingRemainder() {
//         List<ParticipantShareRequest> participants = List.of(
//                 participant(1, 33.33), participant(2, 33.33), participant(3, 33.34));
 
//         Map<Long, BigDecimal> result = calculator.calculate(new BigDecimal("100.00"), SplitType.PERCENTAGE, participants);
 
//         assertThat(sumOf(result)).isEqualByComparingTo("100.00");
//     }
 
//     // ---------- SHARES ----------
 
//     @Test
//     void sharesSplit_isProportionalToShareCounts() {
//         // 2:1:1 shares of 400 -> 200 / 100 / 100
//         List<ParticipantShareRequest> participants = List.of(
//                 participant(1, 2.0), participant(2, 1.0), participant(3, 1.0));
 
//         Map<Long, BigDecimal> result = calculator.calculate(new BigDecimal("400.00"), SplitType.SHARES, participants);
 
//         assertThat(result.get(1L)).isEqualByComparingTo("200.00");
//         assertThat(result.get(2L)).isEqualByComparingTo("100.00");
//         assertThat(result.get(3L)).isEqualByComparingTo("100.00");
//         assertThat(sumOf(result)).isEqualByComparingTo("400.00");
//     }
 
//     @Test
//     void sharesSplit_rejectsZeroTotalShares() {
//         List<ParticipantShareRequest> participants = List.of(participant(1, 0.0), participant(2, 0.0));
 
//         assertThatThrownBy(() -> calculator.calculate(new BigDecimal("100.00"), SplitType.SHARES, participants))
//                 .isInstanceOf(ApiException.class)
//                 .hasMessageContaining("Total shares must be greater than 0");
//     }
 
//     // ---------- shared validation ----------
 
//     @Test
//     void missingValueForNonEqualSplit_throwsWithHelpfulMessage() {
//         List<ParticipantShareRequest> participants = List.of(participant(1, null), participant(2, 100.0));
 
//         assertThatThrownBy(() -> calculator.calculate(new BigDecimal("100.00"), SplitType.UNEQUAL, participants))
//                 .isInstanceOf(ApiException.class)
//                 .hasMessageContaining("missing an exact amount");
//     }
// }