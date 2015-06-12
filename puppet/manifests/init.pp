class disthene (
  $java_xmx = '4G',
  $java_xms = '2G',
  $java_extra_options = '',

  $disthene_reader_package_version = 'present',

  $reader_host = '127.0.0.1',
  $reader_port = '2003',
  $reader_rollups = ['60s:5356800s','900s:62208000s'],
  $reader_resolution = '1600',

  $store_cluster = [$::ipaddress],
  $store_port = '9042',
  $store_keyspace = 'metric',
  $store_column_family = 'metric',
  $store_max_connections = '2048',
  $store_read_timeout = '5',
  $store_connect_timeout = '5',
  $store_max_requests = '128',

  $index_name = 'disthene',
  $index_cluster = [$::ipaddress],
  $index_port = 9300,
  $index_index = 'disthene_paths',
  $index_type = 'path',
  $index_scroll = '50000',
  $index_timeout = '120000',
  $index_bulk_actions = '10000',
  $index_max_paths = '10000',

  $stats_interval = 60,
  $stats_tenant = "NONE",
  $stats_hostname = "$::hostname",
  $stats_log = 'true',

  $custom_log_config = false,
)
{
  if $custom_log_config {
    $disthene_log_config = 'puppet:///modules/config/disthene-reader-log4j.xml'
  }
  else {
    $disthene_log_config = 'puppet:///modules/disthene/disthene-reader-log4j.xml'
  }

  file { 'disthene_reader_config':
    ensure  => present,
    path    => '/etc/disthene-reader/disthene-reader.yaml',
    content => template('disthene-reader/disthene-reader.yaml.erb'),
    require => Package['disthene-reader'],
  }

  file { 'disthene_reader_log_config':
    ensure  => present,
    path    => '/etc/disthene-reader/disthene-reader-log4j.xml',
    source  => $disthene_log_config,
    require => Package['disthene-reader'],
    notify  => Service['disthene-reader'],
  }

  file { 'disthene_reader_defaults':
    ensure  => present,
    path    => '/etc/default/disthene-reader',
    content => template('disthene/disthene-reader-default.erb'),
    require => File['disthene_reader_config'],
  }

  service { 'disthene-reader':
    ensure     => running,
    hasrestart => true,
    restart    => '/etc/init.d/disthene-reader reload',
    require    => [Package['disthene-reader'],
      File['disthene_reader_config'],
    ],
  }
}
