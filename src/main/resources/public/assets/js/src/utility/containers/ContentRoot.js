var React = require('react');
var ReactDOM = require('react-dom');
var { bindActionCreators } = require('redux');
var { connect } = require('react-redux');
var { Link } = require('react-router');
var FormattedMessage = require('react-intl').FormattedMessage;
var Bootstrap = require('react-bootstrap');
var ScrollToTop = require('react-scroll-up');

var LoginForm = require('../components/LoginForm');
var LocaleSwitcher = require('../components/LocaleSwitcher');

var { login, logout } = require('../actions/SessionActions');
var { setLocale } = require('../actions/LocaleActions');

var Collapsible = require('../components/Collapsible');

var expandConsumers= function(e) {
	this.setState({ expandConsumers : !this.state.expandConsumers});
};
 
var expandReports = function(e) {
  this.setState({ expandReports : !this.state.expandReports});
};

var expandSupport = function (e) {
  this.setState({ expandSupport : !this.state.expandSupport});
};

var disableLink = function(e) {
  e.preventDefault();
};

var ContentRoot = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},

	getInitialState() {
		return {
			expandConsumers: false,
			expandReports: false,
			expandSupport: false
    };
	},

	render: function() {
		var content = null;
		var _t = this.context.intl.formatMessage;

		if(!this.props.isAuthenticated) {
			content = (
				<div className='login-wrapper'>
					<nav className='navbar navbar-default navbar-fixed-top'>
						<div className='navbar-header' style={{ paddingLeft: 15}} >
							<a className='navbar-brand' href='#' style={{ padding: 0, margin: 0}}>
								<img alt='DAIAD' src='/assets/images/shared/daiad-logo.svg' style={{ marginTop: 15 }} />
							</a>
						</div>
						<div style={{ float: 'right', marginTop: 8, marginLeft: 10, paddingRight: 15}}>
							<LocaleSwitcher locale={this.props.locale} onLocaleSwitch={this.props.actions.setLocale} />
						</div>
					</nav>
					<div>
						<LoginForm action='login' 	isAuthenticated = { this.props.isAuthenticated } 
													errors = {this.props.session.errors}
													onLogin = {this.props.actions.login}
													isLoading = {this.props.session.isLoading}/>
					</div>
				</div>
			);
		} else {
			content = (
				<div className='wrapper'>
					<nav className='navbar navbar-default navbar-fixed-top'>
						<div className='navbar-header' style={{ paddingLeft: 15 }} >
							<a className='navbar-brand' href='#' style={{ padding: 0, margin: 0}}>
								<img alt='DAIAD' src='/assets/images/shared/daiad-logo.svg' style={{ marginTop: 15 }} />
							</a>
						</div>
						<div style={{ float: 'right', marginTop: 12, marginLeft: 10, paddingRight: 45}}>
						  <span style={{marginRight: 10}}>
						    {this.props.session.username}
						  </span>
						  <i className='fa fa-sign-out fa-fw' style={{color : '#d9534f', cursor : 'pointer'}} onClick={this.props.actions.logout}></i>
						</div>
						<div className='navbar-default navbar-static-side' role='navigation'>
							<div className='sidebar-collapse'>
							  <ul className='nav' id='side-menu'>
        	        <li>
            			  <Link to='/'>
              			  <i className='fa fa-dashboard fa-fw'></i>{' ' + _t({ id: 'Section.Dashboard'})}
              			</Link>
                	</li>
                	<li>
                		<Link to='/analytics'>
                  		<i className='fa fa-bar-chart fa-fw'></i>{' ' + _t({ id: 'Section.Analytics'})}
                		</Link>
            			</li>
                	<li>
                		<Link to='/forecasting' onClick={disableLink.bind(this)} className='disabled-link'>
                			<i className='fa fa-line-chart fa-fw'></i>{' ' + _t({ id: 'Section.Forecasting'})}
              			</Link>
            			</li>
        					<li>
        						<a href='#' onClick={expandConsumers.bind(this)}>
        							<i className='fa fa-group fa-fw'></i>
        							{' ' + _t({ id: 'Section.Consumers'}) + ' '}
        							{ this.state.expandConsumers ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)}
        						</a>
        						<Collapsible open={this.state.expandConsumers}>
        							<ul className='nav'>
                  					<li>
                  						<Link to='/demographics'>
	            							<span  style={{paddingLeft: 18}}>
	            								<i className='fa fa-bookmark fa-fw'></i>{' ' + _t({ id: 'Section.Demographics'})}
            								</span>
	            						</Link>
	            					</li>
	            					<li>
	            						<Link to='/search'>
	            							<span  style={{paddingLeft: 18}}>
	            								<i className='fa fa-search fa-fw'></i>{' ' + _t({ id: 'Section.Search'})}
            								</span>
	            						</Link>
	            					</li>
                  	  </ul>
        						</Collapsible>
        					</li>
          				<li>
          					<Link to='/scheduler'>
          						<i className='fa fa-clock-o fa-fw'></i>{' ' + _t({ id: 'Section.Scheduler'})}
          					</Link>
          				</li>
                  <li>
                    <Link to='/manage-alerts'>
                      <i className='fa fa-commenting-o fa-fw'></i>{' ' + _t({ id: 'Section.ManageAlerts'})}
                    </Link>
                  </li>
                  <li>
                    <Link to='/settings/user'>
                      <i className='fa fa-user fa-fw'></i>{' ' + _t({ id: 'Settings.User'})}
                    </Link>
                  </li>
          				<li>
          				  <a href='#' onClick={expandReports.bind(this)}>
                      <i className='fa fa-flask fa-fw'></i>
                      {' ' + _t({ id: 'Section.Reports.Group'}) + ' '}
                      { this.state.expandReports ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)}
                    </a>
                    <Collapsible open={this.state.expandReports}>
                      <ul className='nav'>
                        <li>
                          <Link to='/report/overview'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-table fa-fw'></i>{' ' + _t({ id: 'Section.Reports.Overview'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/report/charts'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-pie-chart fa-fw'></i>{' ' + _t({ id: 'Section.Reports.Charts'})}
                            </span>
                          </Link>
                        </li>
                      </ul>
                    </Collapsible>
          				</li>
                  <li>
                    <a href='#' onClick={expandSupport.bind(this)}>
                      <i className='fa fa-support fa-fw'></i>
                      {' ' + _t({ id: 'Section.Support.Group'}) + ' '}
                      { this.state.expandSupport ? (<i className='fa fa-caret-up fa-fw'></i>) : (<i className='fa fa-caret-down fa-fw'></i>)}
                    </a>
                    <Collapsible open={this.state.expandSupport}>
                      <ul className='nav'>
                        <li>
                          <Link to='/support/logging'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-history fa-fw'></i>{' ' + _t({ id: 'Section.Support.Logging'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/mode/management'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-sliders fa-fw'></i>{' ' + _t({ id: 'Section.ModeManagement'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/support/data'>
                            <span  style={{paddingLeft: 18}}>
                            <i className='fa fa-database fa-fw'></i>{' ' + _t({ id: 'Section.Support.Data'})}
                            </span>
                          </Link>
                        </li>
                        <li>
                          <Link to='/support/development'>
                            <span  style={{paddingLeft: 18}}>
                              <i className='fa fa-bug fa-fw'></i>{' ' + _t({ id: 'Section.Support.Development'})}
                            </span>
                          </Link>
                        </li>
                      </ul>
                    </Collapsible>
                  </li>            				
  				      </ul>
            	</div>
          	</div>
					</nav>
					<div className='page-wrapper'>
						{this.props.children}
					</div>
					<ScrollToTop showUnder={160}>
						<div style={{marginRight: -25}}>
							<i className='fa fa-arrow-up fa-2x fa-fw' style={{ color : '#337ab7'}}></i>
						</div>
					</ScrollToTop>
				</div>
			);
		}
		
		return content;
  }
});

function mapStateToProps(state) {
	return {
	    isAuthenticated: state.session.isAuthenticated,
	    session: {
	    	errors: state.session.errors,
	    	isLoading: state.session.isLoading,
	    	username: (state.session.profile ? state.session.profile.username : '')
	    },
	    routing: state.routing
	};
}

function mapDispatchToProps(dispatch) {
	return {
		actions : bindActionCreators(Object.assign({}, { login, logout, setLocale}) , dispatch)
	};
}

module.exports = connect(mapStateToProps, mapDispatchToProps)(ContentRoot);
