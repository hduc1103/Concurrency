#Ha Noi wind direction from 2011 to 2023
import pandas as pd
from datetime import datetime
import concurrent.futures
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from bs4 import BeautifulSoup

chrome_options = Options()
chrome_options.add_argument('--headless')
chrome_options.add_argument('--no-sandbox')
chrome_options.add_argument('--disable-dev-shm-usage')

start_date = datetime(2011, 1, 1)
end_date = datetime(2023, 12, 31)
station_id = "488200"
base_url = "https://meteologix.com/vn/observations/vietnam/wind-direction/{}-{}z.html"

urls = [
    base_url.format(date.strftime('%Y%m%d'), f"{hour:02d}00")
    for date in pd.date_range(start_date, end_date)
    for hour in range(24)
]

output_file = "HaNoi_wind_direction_2011_2023.csv"
batch_size = 1000 

def initialize_csv():
    df = pd.DataFrame(columns=["date", "station_id", "time", "wind direction"])
    df.to_csv(output_file, index=False, mode='w')

def fetch_data(url):
    data = []
    driver = webdriver.Chrome(options=chrome_options)
    driver.get(url)
    
    try:
        WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, f"[data-station-id='{station_id}']"))
        )
        soup = BeautifulSoup(driver.page_source, 'html.parser')
        element = soup.find(attrs={"data-station-id": station_id})
        
        if element:
            title = element.get("title")
            date = url.split('/')[-1].split('-')[0]
            station_data = {"date": date, "station_id": station_id}

            if title:
                parts = title.split('|')
                if len(parts) >= 3:
                    time = parts[2].strip()
                    wind_direction = parts[0].strip()
                    station_data.update({"time": time, "wind direction": wind_direction})
                else:
                    station_data.update({"time": None, "wind direction": None})
            else:
                station_data.update({"time": None, "wind direction": None})
            data.append(station_data)
    finally:
        driver.quit()
    return data

def save_batch_to_csv(batch_data):
    df = pd.DataFrame(batch_data)
    df.to_csv(output_file, index=False, mode='a', header=False)  

initialize_csv() 

batch_data = []
with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
    for i, result in enumerate(executor.map(fetch_data, urls), start=1):
        batch_data.extend(result)
        
        if i % batch_size == 0:
            save_batch_to_csv(batch_data)
            batch_data.clear() 

if batch_data:
    save_batch_to_csv(batch_data)
