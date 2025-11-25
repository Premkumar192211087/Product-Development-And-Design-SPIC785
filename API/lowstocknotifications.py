import pandas as pd
from prophet import Prophet
import mysql.connector
from datetime import datetime

# MySQL connection
conn = mysql.connector.connect(
    host="localhost",
    user="root",
    password="",
    database="your_database"
)
cursor = conn.cursor(dictionary=True)

# Fetch distinct store_ids
cursor.execute("SELECT DISTINCT store_id FROM reports WHERE type = 'sold'")
store_ids = [row['store_id'] for row in cursor.fetchall()]

for store_id in store_ids:
    cursor.execute("""
        SELECT timestamp, quantity, product_name
        FROM reports
        WHERE store_id = %s AND type = 'sold'
    """, (store_id,))
    
    data = pd.DataFrame(cursor.fetchall())

    if data.empty:
        continue

    data['timestamp'] = pd.to_datetime(data['timestamp'])
    
    for product in data['product_name'].unique():
        product_data = data[data['product_name'] == product]
        grouped = product_data.groupby(pd.Grouper(key='timestamp', freq='W'))['quantity'].sum().reset_index()
        grouped.columns = ['ds', 'y']

        if len(grouped) < 2:
            continue

        model = Prophet()
        model.fit(grouped)
        future = model.make_future_dataframe(periods=2, freq='W')
        forecast = model.predict(future)

        predicted_qty = int(forecast.iloc[-1]['yhat'])

        # Fetch current stock
        cursor.execute("""
            SELECT quantity FROM products WHERE product_name = %s AND store_id = %s
        """, (product, store_id))
        stock_result = cursor.fetchone()

        if not stock_result:
            continue

        current_qty = int(stock_result['quantity'])

        # Check and insert notification
        if predicted_qty > current_qty:
            title = "Low Stock Alert"
            msg = f"Forecast predicts high demand for '{product}'. Only {current_qty} in stock."
        elif predicted_qty < current_qty / 2:
            title = "Overstock Alert"
            msg = f"'{product}' might be overstocked. Consider reducing restocks."
        else:
            continue

        timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

        cursor.execute("""
            INSERT INTO notifications (store_id, title, message, timestamp)
            VALUES (%s, %s, %s, %s)
        """, (store_id, title, msg, timestamp))

        conn.commit()

cursor.close()
conn.close()
print("Forecasting complete and notifications inserted.")
