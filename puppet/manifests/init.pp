class disthene_reader (
  $java_xmx = '4G',
  $java_xms = '2G',
  $java_extra_options = '',

  $disthene_reader_package_version = 'present',

  $reader_host = '127.0.0.1',
  $reader_port = '8080',
  $reader_threads = '16',
  $reader_request_timeout = '30',
  $reader_max_points = '60000000',
  $reader_rollups = ['60s:5356800s','900s:62208000s'],

  $store_cluster = [$::ipaddress],
  $store_port = '9042',
  $store_keyspace = 'metric',
  $store_column_family = 'metric',
  $store_max_connections = '2048',
  $store_read_timeout = '5',
  $store_connect_timeout = '5',
  $store_max_requests = '128',
  $store_load_balancing_policy = "TokenDcAwareRoundRobinPolicy",
  $store_protocol_version = "V2",
  $store_tenant_keyspace = "metric",
  $store_cache_expiration = 180,
  $store_tenant_table_template = "metric_%s_%d",

  $index_name = 'disthene',
  $index_cluster = [$::ipaddress],
  $index_port = 9300,
  $index_index = 'disthene_paths',
  $index_type = 'path',
  $index_scroll = '50000',
  $index_timeout = '120000',
  $index_max_paths = '50000',

  $stats_interval = 60,
  $stats_tenant = "NONE",
  $stats_hostname = "$::hostname",
  $stats_carbon_hostname = "carbon",
  $stats_carbon_port = 2003,

  $custom_log_config = false,
  $custom_throttling_config = false,
)
{
  if $custom_log_config {
    $disthene_log_config = 'puppet:///modules/config/disthene-reader-log4j.xml'
  }
  else {
    $disthene_log_config = 'puppet:///modules/disthene_reader/disthene-reader-log4j.xml'
  }

  if $custom_throttling_config {
    $disthene_throttling_config = 'puppet:///modules/config/throttling.yaml'
  }
  else {
    $disthene_throttling_config = 'puppet:///modules/disthene_reader/throttling.yaml'
  }

  package { 'disthene-reader':
    ensure => installed,
  }

  file { 'disthene_reader_config':
    ensure  => present,
    path    => '/etc/disthene-reader/disthene-reader.yaml',
    content => template('disthene_reader/disthene-reader.yaml.erb'),
    require => Package['disthene-reader'],
  }

  file { 'disthene_reader_log_config':
    ensure  => present,
    path    => '/etc/disthene-reader/disthene-reader-log4j.xml',
    source  => $disthene_log_config,
    require => Package['disthene-reader'],
    notify  => Service['disthene-reader'],
  }

  file { 'disthene_throttling_config':
    ensure  => present,
    path    => '/etc/disthene-reader/throttling.yaml',
    source  => $disthene_throttling_config,
    require => Package['disthene-reader'],
    notify  => Service['disthene-reader'],
  }

  file { 'disthene_reader_defaults':
    ensure  => present,
    path    => '/etc/default/disthene-reader',
    content => template('disthene_reader/disthene-reader-default.erb'),
    require => File['disthene_reader_config'],
  }

  service { 'disthene-reader':
    ensure     => running,
    hasrestart => true,
    restart    => '/bin/systemctl reload disthene-reader.service',
    require    => [Package['disthene-reader'],
      File['disthene_reader_config'],
    ],
  }
}
