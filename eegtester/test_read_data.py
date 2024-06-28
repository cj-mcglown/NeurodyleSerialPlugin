from time import sleep, time
from serial import Serial
from serial.tools.list_ports import comports
import struct
import datetime as dt
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import collections
from dataclasses import dataclass
import queue
import threading

import dearpygui.dearpygui as dpg

DATA_RATE = 2000

signal_delete = False

plot_idx = 0

ADCData = collections.namedtuple("ADCData", "ch1 ch2 ch3 ch4 ch5 ch6 ch7 ch8")
ADCDataEntry = collections.namedtuple("ADCDataEntry", ["sample_num", "samples"])


# Grab the Device from the port list
SERIAL: None | Serial = None
ports = comports()
for port, _, hwid in ports:
    print(port)
    if "303A:4001" in hwid:
        SERIAL = Serial(port)
        print("Found Serial")

# a data queue for storing up to 3 seconds worth of data
data_queue = queue.Queue(maxsize=3 * DATA_RATE)

data_num = 0

datax = [0]
datay = [0]


def read_data():
    global data_num, SERIAL, data_queue, datax, datay, signal_delete
    while not signal_delete:
        try:
            res = SERIAL.read(4)
            if res == b"DATA":
                data_num += 1
                res = SERIAL.read(16 * 4)
                adc1 = struct.unpack_from("<hhhhhhhh", res, 0)
                adc2 = struct.unpack_from("<hhhhhhhh", res, 16)
                adc3 = struct.unpack_from("<hhhhhhhh", res, 32)
                adc4 = struct.unpack_from("<hhhhhhhh", res, 48)

                # new_data: ADCDataEntry = data_queue.get()
                new_data: ADCDataEntry = ADCDataEntry(
                    data_num, [adc1, adc2, adc3, adc4]
                )
                data_queue.put(new_data)

            else:
                res = SERIAL.read_all()
                print("MISSED DATA")
        except:
            print("Issue with Serial Comm, attempting to reconnect")
            SERIAL.close()
            SERIAL = None
            while SERIAL is None:
                sleep(1)
                for port, _, hwid in ports:
                    print(port)
                    if "303A:4001" in hwid:
                        try:
                            SERIAL = Serial(port)
                            print("Found Serial")
                        except:
                            continue


# setup dearimgui
dpg.create_context()
dpg.create_viewport()
dpg.setup_dearpygui()


def data_worker():
    global datax, datay, data_num, data_queue, signal_delete, plot_idx
    while not signal_delete:
        new_data: ADCDataEntry = data_queue.get()
        data_queue.task_done()
        datax.append(new_data.sample_num)
        idx = int(plot_idx / 8)  # Chip to access
        channel = int(plot_idx % 8)  # channel within the chip to access
        datay.append(new_data.samples[idx][channel])
        datax = datax[-2000:]
        datay = datay[-2000:]
        dpg.set_axis_limits("x_axis", data_num - 2000, data_num)
        dpg.set_value("series_tag", [datax, datay])


def set_data_rate(sender, app_data, user_data):
    rate = user_data[0]
    if SERIAL is not None:
        if rate == 500:
            SERIAL.write(b"DR1")
        if rate == 1000:
            SERIAL.write(b"DR2")
        if rate == 2000:
            SERIAL.write(b"DR3")


with dpg.window(label="Data View"):
    # create plot
    with dpg.plot(label="Line Series", width=1000, height=600):
        dpg.add_plot_axis(dpg.mvXAxis, label="sample number", tag="x_axis")
        dpg.add_plot_axis(dpg.mvYAxis, label="sample value", tag="y_axis")
        dpg.set_axis_limits("y_axis", -0x8000, 0x7FFF)
        dpg.add_line_series(
            datax,
            datay,
            parent="y_axis",
            tag="series_tag",
        )

    def change_plot(sender):
        global plot_idx
        if plot_idx < 31:
            plot_idx += 1
        else:
            plot_idx = 0
        dpg.set_item_label(sender, f"Change Plot [{plot_idx}]:")

    dpg.add_button(
        label="Change Plot [0]", callback=change_plot, tag="btn_ChangeSample"
    )
    dpg.add_button(label="DR 500", user_data=(500,), callback=set_data_rate)
    dpg.add_button(label="DR 1000", user_data=(1000,), callback=set_data_rate)
    dpg.add_button(label="DR 2000", user_data=(2000,), callback=set_data_rate)


read = threading.Thread(target=read_data, daemon=True)
worker = threading.Thread(target=data_worker, daemon=True)
read.start()
worker.start()
dpg.show_viewport()
dpg.start_dearpygui()

signal_delete = True
read.join()
worker.join()
dpg.destroy_context()
