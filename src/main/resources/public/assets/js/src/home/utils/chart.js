var moment = require('moment');
var { getPeriodByTimestamp, getLowerGranularityPeriod } = require('./time');

const getCount = function(metrics) {
  return metrics.count?metrics.count:1;
};

const getTimestampIndex = function(points, timestamp) {
    return points.findIndex((x) => (x[0]===timestamp));
};

const getChartMeterCategories = function(period, intl) {
  if (period === 'year') {
    return Array.from({length: 12}, (v, i) => intl.formatMessage({id:`months.${i}`}));
  }
  else if (period === 'month') {
    return Array.from({length: 4}, (v, i) => `Week ${i+1}`);
  }
  else if (period === 'week') {
    return Array.from({length: 7}, (v, i) => intl.formatMessage({id: `weekdays.${i}`}));
  }
  else if (period === 'day') {
    return Array.from({length: 24}, (v, i) => `${i}:00`);
  }
  else return [];
};

const getChartAmphiroCategories = function(period, offset) {
  if (period === 'ten') {
    return Array.from({length: 10}, (v, i) => `#${offset+i}`);
  }
  else if (period === 'twenty') {
    return Array.from({length: 20}, (v, i) => `#${offset+i}`);
  }
  else if (period === 'fifty') {
    return Array.from({length: 50}, (v, i) => `#${offset+i}`);
  }
  else return [];
};

const getChartTimeDataByFilter = function(data, filter, period, intl) {
  return getChartCategoriesByPeriod(period, intl).map((v, i) =>
            data.find(session => getPeriodByTimestamp(period, session.timestamp) === i) ? (data[i] == null ? null : data[i][filter]) : null);
};

//TODO: have to make sure data is ALWAYS fetched in order of ascending ids for amphiro, ascending timestamps for meters
const getChartDataByFilter = function(data, filter, xAxisData) {
  //if not x axis data then x axis time
  if (xAxisData === null) {
    return data.map(session => session[filter] == null ? [] :
                      [new Date(session.timestamp), session[filter]]);
  }
  //else x axis is category
  else {
    return xAxisData.map((v, i) =>
      data[i] ? data[i][filter] : null);
  }
};


module.exports = {
  getChartTimeDataByFilter,
  getChartDataByFilter,
  getChartMeterCategories,
  getChartAmphiroCategories,
};
