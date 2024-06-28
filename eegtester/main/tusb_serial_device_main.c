/// The Following code used the tusb_serial_device example to get started

#include "esp_log.h"
#include "esp_timer.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "sdkconfig.h"
#include "tinyusb.h"
#include "tusb_cdc_acm.h"
#include <math.h>
#include <stdint.h>

static const char *TAG = "EEG_TestDataMaker";
static uint8_t txbuf[CONFIG_TINYUSB_CDC_TX_BUFSIZE + 1];
static uint8_t rxbuf[CONFIG_TINYUSB_CDC_RX_BUFSIZE + 1];

static uint16_t data_rate = 500; // CHANGE DATA RATE HERE
#define DR_TO_MICRO(dr) ((uint64_t)1000000 / (uint64_t)dr)

static esp_timer_handle_t data_generation_timer;

#define CHIP_1_FREQ (2 * M_PI * 1)
#define CHIP_2_FREQ (2 * M_PI * 5)
#define CHIP_3_FREQ (2 * M_PI * 10)
#define CHIP_4_FREQ (2 * M_PI * 20)
static float curr_time = 0;

typedef struct adc_chip_data_t {
  //   uint32_t status;      // status bits Uncomment to test that
  int16_t channels[8]; // Channels
} adc_chip_data_t;

typedef struct data_packet_t {
  char header[4];           // "DATA"
  adc_chip_data_t chips[4]; // Chip Data
} data_packet_t;

// ******** Functions in File (Ctrl+Left Click should take you to it) *********
void app_main(void);
void send_data(const uint8_t *data_buffer, size_t data_buffer_size);
void generate_data(void *arg);
void init_timer(void);

void tinyusb_cdc_line_state_changed_callback(int itf, cdcacm_event_t *event);
void init_USB(void);
// ****************************************************************************

void generate_data(void *arg /* non Used Arg for Timer Callback */) {
  // Use the TxBuffer as the data structure
  curr_time += (float)(DR_TO_MICRO(data_rate)) / 1000000.0f;
  // This will just make sure the buffer supports the size of the datapacket
  ESP_STATIC_ASSERT(sizeof(data_packet_t) < sizeof(txbuf) / (sizeof(txbuf[0])));
  data_packet_t *data = (data_packet_t *)txbuf;
  data->header[0] = 'D';
  data->header[1] = 'A';
  data->header[2] = 'T';
  data->header[3] = 'A';

  //   // Uncomment to test status bits
  //   data->chips[0].status = 'STAT';
  //   data->chips[1].status = 'STAT';
  //   data->chips[2].status = 'STAT';
  //   data->chips[3].status = 'STAT';

  int16_t chip1_val = sinf(CHIP_1_FREQ * curr_time) * (0x7FFF / 8);
  int16_t chip2_val = sinf(CHIP_2_FREQ * curr_time) * (0x7FFF / 8);
  int16_t chip3_val = sinf(CHIP_3_FREQ * curr_time) * (0x7FFF / 8);
  int16_t chip4_val = sinf(CHIP_4_FREQ * curr_time) * (0x7FFF / 8);
  data->chips[0].channels[0] = 1 * chip1_val;
  data->chips[0].channels[1] = 2 * chip1_val;
  data->chips[0].channels[2] = 3 * chip1_val;
  data->chips[0].channels[3] = 4 * chip1_val;
  data->chips[0].channels[4] = 5 * chip1_val;
  data->chips[0].channels[5] = 6 * chip1_val;
  data->chips[0].channels[6] = 7 * chip1_val;
  data->chips[0].channels[7] = 8 * chip1_val;

  data->chips[1].channels[0] = 1 * chip2_val;
  data->chips[1].channels[1] = 2 * chip2_val;
  data->chips[1].channels[2] = 3 * chip2_val;
  data->chips[1].channels[3] = 4 * chip2_val;
  data->chips[1].channels[4] = 5 * chip2_val;
  data->chips[1].channels[5] = 6 * chip2_val;
  data->chips[1].channels[6] = 7 * chip2_val;
  data->chips[1].channels[7] = 8 * chip2_val;

  data->chips[2].channels[0] = 1 * chip3_val;
  data->chips[2].channels[1] = 2 * chip3_val;
  data->chips[2].channels[2] = 3 * chip3_val;
  data->chips[2].channels[3] = 4 * chip3_val;
  data->chips[2].channels[4] = 5 * chip3_val;
  data->chips[2].channels[5] = 6 * chip3_val;
  data->chips[2].channels[6] = 7 * chip3_val;
  data->chips[2].channels[7] = 8 * chip3_val;

  data->chips[3].channels[0] = 1 * chip4_val;
  data->chips[3].channels[1] = 2 * chip4_val;
  data->chips[3].channels[2] = 3 * chip4_val;
  data->chips[3].channels[3] = 4 * chip4_val;
  data->chips[3].channels[4] = 5 * chip4_val;
  data->chips[3].channels[5] = 6 * chip4_val;
  data->chips[3].channels[6] = 7 * chip4_val;
  data->chips[3].channels[7] = 8 * chip4_val;

  send_data(txbuf, sizeof(data_packet_t));
}

// Send Data over usb
void send_data(const uint8_t *data_buffer, size_t data_buffer_size) {
  tinyusb_cdcacm_write_queue(TINYUSB_CDC_ACM_0, data_buffer, data_buffer_size);
  tinyusb_cdcacm_write_flush(TINYUSB_CDC_ACM_0, 0);
}

// To handle any recieve commands (Such as from a terminal)
void tinyusb_cdc_rx_callback(int itf, cdcacm_event_t *event) {
  esp_timer_stop(data_generation_timer);
  size_t rx_size = 0;

  if (ESP_OK == tinyusb_cdcacm_read(itf, rxbuf, CONFIG_TINYUSB_CDC_RX_BUFSIZE,
                                    &rx_size)) {

    ESP_LOG_BUFFER_HEXDUMP(TAG, rxbuf, rx_size, ESP_LOG_INFO);
    if (rx_size >= 3) {
      if (rxbuf[0] == 'D' && rxbuf[1] == 'R') {
        // Hard Coded available options
        switch (rxbuf[2]) {
        case '1':
          data_rate = 500;
          break;
        case '2':
          data_rate = 1000;
          break;
        case '3':
          data_rate = 2000;
          break;

        default:
          break;
        }
      }
    } else {
      const char *msg =
          "Only available Command format is: \"DR1\", \"DR2\", \"DR3\" "
          "for 500, 1000, and 2000 SPS respectively\n";
      tinyusb_cdcacm_write_queue(itf, (uint8_t *)msg, 96);
      tinyusb_cdcacm_write_flush(itf, 0);
    }
  } else {
    ESP_LOGE(TAG, "Read error");
  }
  esp_timer_start_periodic(data_generation_timer, DR_TO_MICRO(data_rate));
}

// Debugging to show that a USB is connecte properly
void tinyusb_cdc_line_state_changed_callback(int itf, cdcacm_event_t *event) {
  int dtr = event->line_state_changed_data.dtr;
  int rts = event->line_state_changed_data.rts;
  ESP_LOGI(TAG, "Line state changed on channel %d: DTR:%d, RTS:%d", itf, dtr,
           rts);
}

// The following Code was directly from the example to initialize the usb
void init_USB(void) {
  ESP_LOGI(TAG, "USB initialization");
  const tinyusb_config_t tusb_cfg = {
      .device_descriptor = NULL,
      .string_descriptor = NULL,
      .external_phy = false,
      .configuration_descriptor = NULL,
  };

  ESP_ERROR_CHECK(tinyusb_driver_install(&tusb_cfg));

  tinyusb_config_cdcacm_t acm_cfg = {
      .usb_dev = TINYUSB_USBDEV_0,
      .cdc_port = TINYUSB_CDC_ACM_0,
      .rx_unread_buf_sz = 64,
      .callback_rx =
          &tinyusb_cdc_rx_callback, // the first way to register a callback
      .callback_rx_wanted_char = NULL,
      .callback_line_state_changed = NULL,
      .callback_line_coding_changed = NULL};

  ESP_ERROR_CHECK(tusb_cdc_acm_init(&acm_cfg));
  /* the second way to register a callback */
  ESP_ERROR_CHECK(tinyusb_cdcacm_register_callback(
      TINYUSB_CDC_ACM_0, CDC_EVENT_LINE_STATE_CHANGED,
      &tinyusb_cdc_line_state_changed_callback));

#if (CONFIG_TINYUSB_CDC_COUNT > 1)
  acm_cfg.cdc_port = TINYUSB_CDC_ACM_1;
  ESP_ERROR_CHECK(tusb_cdc_acm_init(&acm_cfg));
  ESP_ERROR_CHECK(tinyusb_cdcacm_register_callback(
      TINYUSB_CDC_ACM_1, CDC_EVENT_LINE_STATE_CHANGED,
      &tinyusb_cdc_line_state_changed_callback));
#endif

  ESP_LOGI(TAG, "USB initialization DONE");
}

void app_main(void) {

  // Init USB
  init_USB();

  // create timer
  const esp_timer_create_args_t periodic_timer_args = {
      .callback = &generate_data, .name = "data generation"};
  esp_timer_create(&periodic_timer_args, &data_generation_timer);
  esp_timer_start_periodic(data_generation_timer, DR_TO_MICRO(data_rate));

  while (1) {
    vTaskDelay(portTICK_PERIOD_MS * 10);
  }
}
