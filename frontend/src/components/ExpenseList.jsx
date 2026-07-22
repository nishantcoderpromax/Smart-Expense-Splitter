import { deleteExpense } from "../api/expenseApi";

export default function ExpenseList({ groupId, expenses, onChanged }) {
  const handleDelete = async (id) => {
    await deleteExpense(groupId, id);
    onChanged();
  };

  return (
    <div>
      <h3>Expenses</h3>
      {expenses.length === 0 && <p>No expenses yet.</p>}
      <ul>
        {expenses.map((e) => (
          <li key={e.id}>
            <strong>{e.description}</strong> — {e.amount} ({e.splitType}
            {e.categoryName ? `, ${e.categoryName}` : ""}), paid by {e.paidByName}
            <button onClick={() => handleDelete(e.id)}>Delete</button>
            <ul>
              {e.shares.map((s) => (
                <li key={s.userId}>{s.name} owes {s.owedAmount}</li>
              ))}
            </ul>
          </li>
        ))}
      </ul>
    </div>
  );
}

